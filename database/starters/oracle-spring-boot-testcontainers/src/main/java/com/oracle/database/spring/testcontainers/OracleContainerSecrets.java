// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Shared, auto-closeable secret files for Oracle AI Database Free containers. */
public final class OracleContainerSecrets implements AutoCloseable {

    /** Documented secret name containing the Oracle administrative password. */
    public static final String ORACLE_PASSWORD_SECRET = "oracle_pwd";
    // The image starts as the oracle user, so owner-only root permissions would make
    // the documented /run/secrets/oracle_pwd unreadable during database setup.
    static final int SECRET_FILE_MODE = 0444;

    private final String oraclePassword;
    private final Map<String, byte[]> secrets = new LinkedHashMap<>();
    private boolean closed;

    private OracleContainerSecrets(String oraclePassword) {
        OracleFreeContainer.requireValidPassword(oraclePassword);
        this.oraclePassword = oraclePassword;
        secrets.put(ORACLE_PASSWORD_SECRET, oraclePassword.getBytes(StandardCharsets.UTF_8));
    }

    /** Creates secrets containing the mandatory {@code oracle_pwd} file. */
    public static OracleContainerSecrets withOraclePassword(String oraclePassword) {
        return new OracleContainerSecrets(oraclePassword);
    }

    /**
     * Adds a supplementary documented secret, such as {@code oracle_pwd_priv_key}.
     * The supplied bytes are copied and wiped when this handle closes.
     */
    public OracleContainerSecrets withSecret(String name, byte[] content) {
        requireOpen();
        if (name == null || !name.matches("[A-Za-z0-9_.-]+")) {
            throw new IllegalArgumentException("Secret name must contain only letters, numbers, dots, underscores, or hyphens");
        }
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Secret content cannot be null or empty");
        }
        byte[] previous = secrets.put(name, content.clone());
        if (previous != null) {
            Arrays.fill(previous, (byte) 0);
        }
        return this;
    }

    /** Returns the Oracle password used for JDBC connections. */
    public String getOraclePassword() {
        requireOpen();
        return oraclePassword;
    }

    Set<String> getSecretNames() {
        requireOpen();
        return Set.copyOf(secrets.keySet());
    }

    byte[] getSecretBytes(String name) {
        requireOpen();
        return secrets.get(name).clone();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        secrets.values().forEach(value -> Arrays.fill(value, (byte) 0));
        secrets.clear();
        closed = true;
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("Oracle container secrets have been closed");
        }
    }
}
