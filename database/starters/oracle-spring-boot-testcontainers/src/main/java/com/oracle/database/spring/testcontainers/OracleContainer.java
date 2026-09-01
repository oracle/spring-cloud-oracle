// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.testcontainers;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers implementation for the official Oracle AI Database Free image.
 *
 * <p>The image provides the {@code SYS}, {@code SYSTEM}, and {@code PDBADMIN}
 * accounts. This container creates a {@code TEST} application user in
 * {@code FREEPDB1} by default.</p>
 *
 * <p>Supported image: {@code container-registry.oracle.com/database/free}</p>
 *
 * <p>Exposed port: 1521</p>
 */
public class OracleContainer extends JdbcDatabaseContainer<OracleContainer> {

    /** Official Oracle AI Database Free container image. */
    public static final String IMAGE_NAME = "container-registry.oracle.com/database/free";
    /** Default image tag. */
    public static final String DEFAULT_TAG = "latest-lite";
    /** Oracle listener port. */
    public static final int ORACLE_PORT = 1521;
    /** Default pluggable database service name. */
    public static final String DEFAULT_DATABASE_NAME = "FREEPDB1";
    /** Default container database SID. */
    public static final String DEFAULT_SID = "FREE";
    /** Default application user. */
    public static final String DEFAULT_USERNAME = "TEST";
    /** Default application and administrator password. */
    public static final String DEFAULT_PASSWORD = "TestPassword1";
    /** Default role granted to the application user. */
    public static final String DEFAULT_APP_USER_ROLE = "DB_DEVELOPER_ROLE";

    private static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse(IMAGE_NAME);
    private static final int DEFAULT_STARTUP_TIMEOUT_MINUTES = 10;
    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 120;
    private static final String SYSTEM_USERNAME = "SYSTEM";
    private static final String PDBADMIN_USERNAME = "PDBADMIN";
    private static final String APP_USER_STARTUP_SCRIPT =
            "/opt/oracle/scripts/startup/01_testcontainers_app_user.sql";
    private static final String DATABASE_READY_LOG = ".*DATABASE IS READY TO USE!.*\\s";
    private static final String APP_USER_READY_LOG = ".*TESTCONTAINERS APP USER IS READY.*\\s";
    private static final String ORACLE_IDENTIFIER_PATTERN = "[A-Z][A-Z0-9_$#]{0,127}";

    private String username = DEFAULT_USERNAME;
    private String appUserPassword = DEFAULT_PASSWORD;
    private String adminPassword = DEFAULT_PASSWORD;
    private List<String> appUserRoles = List.of(DEFAULT_APP_USER_ROLE);
    private boolean appUser = true;
    private boolean usingSid;

    /** Creates a container using the official image and default tag. */
    public OracleContainer() {
        this(IMAGE_NAME + ":" + DEFAULT_TAG);
    }

    /**
     * Creates a container using an image name.
     *
     * @param dockerImageName compatible Oracle AI Database Free image name
     */
    public OracleContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    /**
     * Creates a container using a parsed image name.
     *
     * @param dockerImageName compatible Oracle AI Database Free image name
     */
    public OracleContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE);
        addExposedPort(ORACLE_PORT);
        waitingFor(waitForLogMessage(APP_USER_READY_LOG));
        withConnectTimeoutSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS);
    }

    @Override
    protected void configure() {
        withEnv("ORACLE_PWD", adminPassword);
        if (appUser) {
            withCopyToContainer(Transferable.of(createAppUserScript()), APP_USER_STARTUP_SCRIPT);
        }
    }

    @Override
    protected void waitUntilContainerStarted() {
        getWaitStrategy().waitUntilReady(this);
    }

    @Override
    public Set<Integer> getLivenessCheckPortNumbers() {
        return Collections.singleton(getMappedPort(ORACLE_PORT));
    }

    @Override
    public String getDriverClassName() {
        return "oracle.jdbc.OracleDriver";
    }

    @Override
    public String getJdbcUrl() {
        String address = getHost() + ":" + getOraclePort();
        return usingSid
                ? "jdbc:oracle:thin:@" + address + ":" + DEFAULT_SID
                : "jdbc:oracle:thin:@//" + address + "/" + DEFAULT_DATABASE_NAME;
    }

    @Override
    public String getUsername() {
        return usingSid ? SYSTEM_USERNAME : username;
    }

    @Override
    public String getPassword() {
        return appUser ? appUserPassword : adminPassword;
    }

    @Override
    public String getDatabaseName() {
        return DEFAULT_DATABASE_NAME;
    }

    @Override
    public OracleContainer withUsername(String username) {
        requireNotBlank(username, "Username");
        String normalizedUsername = username.toUpperCase(Locale.ROOT);
        if ("SYS".equals(normalizedUsername)) {
            throw new IllegalArgumentException("SYS connections require SYSDBA privileges");
        }

        usingSid = false;
        appUser = !SYSTEM_USERNAME.equals(normalizedUsername) && !PDBADMIN_USERNAME.equals(normalizedUsername);
        if (appUser && !normalizedUsername.matches(ORACLE_IDENTIFIER_PATTERN)) {
            throw new IllegalArgumentException("Username must be a valid unquoted Oracle AI Database identifier");
        }

        this.username = normalizedUsername;
        waitingFor(waitForLogMessage(appUser ? APP_USER_READY_LOG : DATABASE_READY_LOG));
        return self();
    }

    @Override
    public OracleContainer withPassword(String password) {
        requireNotBlank(password, "Password");
        requireValidPassword(password);
        if (appUser) {
            this.appUserPassword = password;
        } else {
            this.adminPassword = password;
        }
        return self();
    }

    /**
     * Sets the password used by administrative accounts.
     *
     * @param adminPassword administrator password
     * @return this container
     */
    public OracleContainer withAdminPassword(String adminPassword) {
        requireNotBlank(adminPassword, "Admin password");
        requireValidPassword(adminPassword);
        this.adminPassword = adminPassword;
        return self();
    }

    /**
     * Replaces the roles granted to the application user.
     *
     * <p>Role names are normalized to uppercase, validated as unquoted Oracle
     * identifiers, and deduplicated. The application user always receives the
     * {@code CREATE SESSION} system privilege. Pass no roles to grant only that
     * privilege.</p>
     *
     * @param roles application-user roles
     * @return this container
     */
    public OracleContainer withAppUserRoles(String... roles) {
        if (roles == null) {
            throw new IllegalArgumentException("Application user roles cannot be null");
        }

        Set<String> normalizedRoles = new LinkedHashSet<>();
        for (String role : roles) {
            requireNotBlank(role, "Application user role");
            String normalizedRole = role.toUpperCase(Locale.ROOT);
            if (!normalizedRole.matches(ORACLE_IDENTIFIER_PATTERN)) {
                throw new IllegalArgumentException(
                        "Application user role must be a valid unquoted Oracle AI Database identifier");
            }
            normalizedRoles.add(normalizedRole);
        }
        appUserRoles = List.copyOf(normalizedRoles);
        return self();
    }

    @Override
    public OracleContainer withDatabaseName(String databaseName) {
        requireNotBlank(databaseName, "Database name");
        if (!DEFAULT_DATABASE_NAME.equalsIgnoreCase(databaseName)) {
            throw new IllegalArgumentException(
                    "The Oracle AI Database Free PDB name is fixed as " + DEFAULT_DATABASE_NAME);
        }
        return self();
    }

    /**
     * Uses a SID-style connection to {@code FREE} as {@code SYSTEM} instead of
     * connecting to the {@code FREEPDB1} service.
     *
     * @return this container
     */
    public OracleContainer usingSid() {
        usingSid = true;
        appUser = false;
        username = SYSTEM_USERNAME;
        waitingFor(waitForLogMessage(DATABASE_READY_LOG));
        return self();
    }

    /**
     * Configures the database character set.
     *
     * @param characterSet Oracle character set name
     * @return this container
     */
    public OracleContainer withCharacterSet(String characterSet) {
        requireNotBlank(characterSet, "Character set");
        return withEnv("ORACLE_CHARACTERSET", characterSet);
    }

    /**
     * Enables or disables archive logging.
     *
     * @param enabled whether archive logging is enabled
     * @return this container
     */
    public OracleContainer withArchiveLog(boolean enabled) {
        return withEnv("ENABLE_ARCHIVELOG", Boolean.toString(enabled));
    }

    /**
     * Enables or disables force logging.
     *
     * @param enabled whether force logging is enabled
     * @return this container
     */
    public OracleContainer withForceLogging(boolean enabled) {
        return withEnv("ENABLE_FORCE_LOGGING", Boolean.toString(enabled));
    }

    /**
     * Returns the mapped Oracle listener port.
     *
     * @return mapped listener port
     */
    public Integer getOraclePort() {
        return getMappedPort(ORACLE_PORT);
    }

    @Override
    protected String getTestQueryString() {
        return "SELECT 1 FROM DUAL";
    }

    @Override
    public OracleContainer withUrlParam(String paramName, String paramValue) {
        throw new UnsupportedOperationException("Oracle JDBC URL parameters are not supported");
    }

    private static void requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be null or blank");
        }
    }

    private static void requireValidPassword(String password) {
        if (password.indexOf('"') >= 0 || password.indexOf('\n') >= 0 || password.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Password cannot contain double quotes or line breaks");
        }
    }

    private String createAppUserScript() {
        String escapedPassword = appUserPassword.replace("'", "''");
        String roleGrant = appUserRoles.isEmpty()
                ? ""
                : "GRANT %s TO %s;%n".formatted(String.join(", ", appUserRoles), username);
        return """
                WHENEVER SQLERROR EXIT SQL.SQLCODE
                ALTER SESSION SET CONTAINER=FREEPDB1;
                DECLARE
                    USER_COUNT NUMBER;
                    TABLESPACE_COUNT NUMBER;
                BEGIN
                    SELECT COUNT(*) INTO TABLESPACE_COUNT FROM DBA_TABLESPACES WHERE TABLESPACE_NAME = 'USERS';
                    IF TABLESPACE_COUNT = 0 THEN
                        EXECUTE IMMEDIATE 'CREATE TABLESPACE USERS DATAFILE ''/opt/oracle/oradata/FREE/FREEPDB1/users01.dbf'' SIZE 100M AUTOEXTEND ON NEXT 100M MAXSIZE UNLIMITED';
                    END IF;

                    SELECT COUNT(*) INTO USER_COUNT FROM DBA_USERS WHERE USERNAME = '%s';
                    IF USER_COUNT = 0 THEN
                        EXECUTE IMMEDIATE 'CREATE USER %s IDENTIFIED BY "%s" DEFAULT TABLESPACE USERS QUOTA UNLIMITED ON USERS';
                    ELSE
                        EXECUTE IMMEDIATE 'ALTER USER %s IDENTIFIED BY "%s" ACCOUNT UNLOCK';
                    END IF;
                END;
                /
                GRANT CREATE SESSION TO %s;
                %s\
                PROMPT TESTCONTAINERS APP USER IS READY
                EXIT;
                """.formatted(
                username,
                username,
                escapedPassword,
                username,
                escapedPassword,
                username,
                roleGrant);
    }

    private static WaitStrategy waitForLogMessage(String message) {
        return Wait.forLogMessage(message, 1)
                .withStartupTimeout(Duration.ofMinutes(DEFAULT_STARTUP_TIMEOUT_MINUTES));
    }
}
