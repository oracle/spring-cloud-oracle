// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.testcontainers;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

/**
 * Common Testcontainers support for the official Oracle AI Database Free image.
 *
 * <p>This class deliberately contains only image and credential mechanics shared by a
 * primary database and a True Cache instance. Database-specific startup and JDBC policy
 * remains in the concrete containers.</p>
 *
 * @param <SELF> concrete container type
 */
public abstract class OracleFreeContainer<SELF extends OracleFreeContainer<SELF>>
        extends JdbcDatabaseContainer<SELF> {

    /** Official Oracle AI Database Free container image. */
    public static final String IMAGE_NAME = "container-registry.oracle.com/database/free";
    /** Oracle listener port. */
    public static final int ORACLE_PORT = 1521;
    /** Default pluggable database service name. */
    public static final String DEFAULT_DATABASE_NAME = "FREEPDB1";
    /** Default container database SID. */
    public static final String DEFAULT_SID = "FREE";
    /** Default administrator password used by the test containers. */
    public static final String DEFAULT_PASSWORD = "TestPassword1";

    private static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse(IMAGE_NAME);
    protected static final String ORACLE_PASSWORD_ENV = "ORACLE_PWD";
    private static final String ORACLE_SECRET_DIRECTORY = "/run/secrets/";
    private static final String ORACLE_IDENTIFIER_PATTERN = "[A-Z][A-Z0-9_$#]{0,127}";

    private String adminPassword = DEFAULT_PASSWORD;
    private OracleContainerSecrets secrets;

    protected OracleFreeContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE);
        addExposedPort(ORACLE_PORT);
    }

    /**
     * Sets the administrative password passed to the image through {@code ORACLE_PWD}.
     *
     * @param password administrative password
     * @return this container
     */
    public SELF withAdminPassword(String password) {
        requireValidPassword(password, "Admin password");
        adminPassword = password;
        secrets = null;
        return self();
    }

    /** Sets the administrator password used by a container without an application-user mode. */
    @Override
    public SELF withPassword(String password) {
        return withAdminPassword(password);
    }

    /**
     * Supplies the image credentials as files beneath {@code /run/secrets}.
     *
     * <p>This is Docker-API portable equivalent of the documented Podman secret names.
     * The password is not added to the container environment. Keep the supplied handle
     * open until all containers using it have started.</p>
     *
     * @param secrets shared secret material
     * @return this container
     */
    public SELF withOracleSecrets(OracleContainerSecrets secrets) {
        if (secrets == null) {
            throw new IllegalArgumentException("Oracle container secrets cannot be null");
        }
        adminPassword = secrets.getOraclePassword();
        this.secrets = secrets;
        return self();
    }

    /** Enables or disables archive logging. */
    public SELF withArchiveLog(boolean enabled) {
        return withEnv("ENABLE_ARCHIVELOG", Boolean.toString(enabled));
    }

    /** Enables or disables force logging. */
    public SELF withForceLogging(boolean enabled) {
        return withEnv("ENABLE_FORCE_LOGGING", Boolean.toString(enabled));
    }

    /** Returns the mapped Oracle listener port. */
    public Integer getOraclePort() {
        return getMappedPort(ORACLE_PORT);
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
    public String getPassword() {
        return adminPassword;
    }

    @Override
    protected void waitUntilContainerStarted() {
        getWaitStrategy().waitUntilReady(this);
    }

    @Override
    protected void configure() {
        if (secrets == null) {
            withEnv(ORACLE_PASSWORD_ENV, adminPassword);
            return;
        }
        for (String secretName : secrets.getSecretNames()) {
            withCopyToContainer(
                    Transferable.of(secrets.getSecretBytes(secretName), OracleContainerSecrets.SECRET_FILE_MODE),
                    ORACLE_SECRET_DIRECTORY + secretName);
        }
    }

    protected static void requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be null or blank");
        }
    }

    protected final boolean hasOracleSecrets() {
        return secrets != null;
    }

    protected static void requireValidPassword(String password) {
        requireValidPassword(password, "Password");
    }

    protected static void requireValidPassword(String password, String name) {
        requireNotBlank(password, name);
        if (password.indexOf('"') >= 0 || password.indexOf('\n') >= 0 || password.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Password cannot contain double quotes or line breaks");
        }
    }

    protected static String normalizeOracleIdentifier(String value, String name) {
        String normalized = value == null ? null : value.toUpperCase(Locale.ROOT);
        requireNotBlank(normalized, name);
        if (!isValidOracleIdentifier(normalized)) {
            throw new IllegalArgumentException(name + " must be a valid unquoted Oracle AI Database identifier");
        }
        return normalized;
    }

    protected static boolean isValidOracleIdentifier(String value) {
        return value != null && value.matches(ORACLE_IDENTIFIER_PATTERN);
    }

    @Override
    protected String getTestQueryString() {
        return "SELECT 1 FROM DUAL";
    }
}
