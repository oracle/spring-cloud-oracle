// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.cloud.stream.binder.sample;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binding.BindingsLifecycleController;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.MountableFile;

@SpringBootTest
@Testcontainers
public class TxEventQSampleAppTest {
    @Container
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.6-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withUsername("testuser")
            .withPassword("testpwd");

    @BeforeAll
    public static void setUp() throws Exception {
        oracleContainer.start();
        oracleContainer.copyFileToContainer(MountableFile.forClasspathResource("init.sql"), "/tmp/init.sql");
        oracleContainer.execInContainer("sqlplus", "sys / as sysdba", "@/tmp/init.sql");
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("JDBC_URL", oracleContainer::getJdbcUrl);
        registry.add("USERNAME", oracleContainer::getUsername);
        registry.add("PASSWORD", oracleContainer::getPassword);
    }

    @Autowired
    WordSupplier wordSupplier;

    @Autowired
    BindingsLifecycleController lifecycleController;

    @Test
    void processStream() throws InterruptedException {
        // Process all words from the word supplier message stream.
        do {
            Thread.sleep(100);
        } while (!wordSupplier.done());

        // Shutdown all messaging beans.
        lifecycleController.queryStates().forEach((state) ->
                lifecycleController.stop((String) state.get("bindingName")));
    }
}
