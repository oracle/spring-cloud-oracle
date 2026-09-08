// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.testcontainers;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
@SuppressFBWarnings(value = {"EQ_DOESNT_OVERRIDE_EQUALS", "VA_FORMAT_STRING_USES_NEWLINE"},
        justification = "Testcontainers use identity equality from GenericContainer, and SQL startup scripts intentionally use text-block newlines.")
public class OracleContainer extends OracleFreeContainer<OracleContainer> {

    /** Default image tag. */
    public static final String DEFAULT_TAG = "latest-lite";
    /** Default application user. */
    public static final String DEFAULT_USERNAME = "TEST";
    /** Default role granted to the application user. */
    public static final String DEFAULT_APP_USER_ROLE = "DB_DEVELOPER_ROLE";

    private static final int DEFAULT_STARTUP_TIMEOUT_MINUTES = 10;
    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 120;
    private static final String SYSTEM_USERNAME = "SYSTEM";
    private static final String PDBADMIN_USERNAME = "PDBADMIN";
    private static final String APP_USER_STARTUP_SCRIPT =
            "/opt/oracle/scripts/startup/01_testcontainers_app_user.sql";
    private static final String DATABASE_READY_LOG = ".*DATABASE IS READY TO USE!.*\\s";
    private static final String APP_USER_READY_LOG = ".*TESTCONTAINERS APP USER IS READY.*\\s";

    private String username = DEFAULT_USERNAME;
    private String appUserPassword = DEFAULT_PASSWORD;
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
        waitingFor(waitForLogMessage(APP_USER_READY_LOG));
        withConnectTimeoutSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS);
    }

    @Override
    protected void configure() {
        super.configure();
        if (appUser) {
            withCopyToContainer(Transferable.of(createAppUserScript()), APP_USER_STARTUP_SCRIPT);
        }
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
        return appUser ? appUserPassword : super.getPassword();
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
        if (appUser && !isValidOracleIdentifier(normalizedUsername)) {
            throw new IllegalArgumentException("Username must be a valid unquoted Oracle AI Database identifier");
        }

        this.username = normalizedUsername;
        waitingFor(waitForLogMessage(appUser ? APP_USER_READY_LOG : DATABASE_READY_LOG));
        return self();
    }

    @Override
    public OracleContainer withPassword(String password) {
        if (appUser) {
            requireValidPassword(password);
            this.appUserPassword = password;
        } else {
            withAdminPassword(password);
        }
        return self();
    }

    /**
     * Sets the password used by administrative accounts.
     *
     * @param adminPassword administrator password
     * @return this container
     */
    @Override
    public OracleContainer withAdminPassword(String adminPassword) {
        return super.withAdminPassword(adminPassword);
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
            normalizedRoles.add(normalizeOracleIdentifier(role, "Application user role"));
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

    @Override
    public OracleContainer withUrlParam(String paramName, String paramValue) {
        throw new UnsupportedOperationException("Oracle JDBC URL parameters are not supported");
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
