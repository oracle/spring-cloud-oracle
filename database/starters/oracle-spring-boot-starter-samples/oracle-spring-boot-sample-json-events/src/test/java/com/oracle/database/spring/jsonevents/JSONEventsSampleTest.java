// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.MountableFile;

@Testcontainers
@SpringBootTest
@Sql("/init.sql") // Initialize the app tables
public class JSONEventsSampleTest {
    /**
     * The Testcontainers Oracle Free module let's us create an Oracle database container in a junit context.
     */
    @Container
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.5-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withUsername("testuser")
            .withPassword(("testpwd"));

    /**
     * Dynamically configure Spring Boot properties to use the Testcontainers database.
     */
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("JDBC_URL", oracleContainer::getJdbcUrl);
        registry.add("USERNAME", oracleContainer::getUsername);
        registry.add("PASSWORD", oracleContainer::getPassword);
    }

    @BeforeAll
    public static void setUp() throws Exception {
        // Run the okafka.sql grants as sysdba on the database test container
        oracleContainer.start();
        oracleContainer.copyFileToContainer(MountableFile.forClasspathResource("okafka.sql"), "/tmp/okafka.sql");
        oracleContainer.execInContainer("sqlplus", "sys / as sysdba", "@/tmp/okafka.sql");
    }


    @Test
    void jsonEventsSampleAppTest() {
    }

}
