/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.RegionProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class OracleGenAiAuthenticationPropertiesTests {

    @TempDir
    Path tempDir;

    @Test
    void createsConfigFileProvider() throws Exception {
        OracleGenAiAuthenticationProperties properties = new OracleGenAiAuthenticationProperties();
        properties.setConfigFile(writeConfig("TEST").toString());
        properties.setProfile("TEST");

        BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();

        assertThat(provider).isInstanceOf(ConfigFileAuthenticationDetailsProvider.class);
        assertThat(provider.getKeyId()).isEqualTo("ocid1.tenancy.oc1..test/ocid1.user.oc1..test/aa:bb:cc");
        assertThat(((RegionProvider) provider).getRegion()).isEqualTo(Region.US_PHOENIX_1);
        assertThat(provider.getPrivateKey()).isNotNull();
    }

    @Test
    void defaultsToDefaultProfileForConfigFileProvider() throws Exception {
        OracleGenAiAuthenticationProperties properties = new OracleGenAiAuthenticationProperties();
        properties.setConfigFile(writeConfig("DEFAULT").toString());

        BasicAuthenticationDetailsProvider provider = properties.createBasicAuthenticationDetailsProvider();

        assertThat(provider).isInstanceOf(ConfigFileAuthenticationDetailsProvider.class);
        assertThat(provider.getKeyId()).isEqualTo("ocid1.tenancy.oc1..test/ocid1.user.oc1..test/aa:bb:cc");
    }

    private Path writeConfig(String profile) throws IOException, GeneralSecurityException {
        Path privateKey = writePrivateKey();
        Path config = tempDir.resolve("config");
        Files.writeString(config, """
                [%s]
                user=ocid1.user.oc1..test
                fingerprint=aa:bb:cc
                tenancy=ocid1.tenancy.oc1..test
                region=us-phoenix-1
                key_file=%s
                """.formatted(profile, privateKey), StandardCharsets.UTF_8);
        return config;
    }

    private Path writePrivateKey() throws IOException, GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        String encoded = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
                .encodeToString(keyPair.getPrivate().getEncoded());
        Path privateKey = tempDir.resolve("oci_api_key.pem");
        Files.writeString(privateKey, """
                -----BEGIN PRIVATE KEY-----
                %s
                -----END PRIVATE KEY-----
                """.formatted(encoded), StandardCharsets.US_ASCII);
        return privateKey;
    }
}
