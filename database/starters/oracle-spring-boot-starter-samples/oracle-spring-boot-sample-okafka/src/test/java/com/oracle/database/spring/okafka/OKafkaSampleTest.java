// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.okafka;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import oracle.jdbc.pool.OracleDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.MountableFile;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class OKafkaSampleTest {
    // Oracle Database 23ai Free container image
    private static final String oracleImage = "gvenzl/oracle-free:23.26.0-slim-faststart";
    private static final String testUser = "testuser";
    private static final String testPassword = "Welcome123#";

    private static OracleDataSource dataSource;

    @Container
    private static final OracleContainer oracleContainer = new OracleContainer(oracleImage)
            .withStartupTimeout(Duration.ofMinutes(3)) // allow possible slow startup
            .withUsername(testUser)
            .withPassword(testPassword);

    @BeforeAll
    static void setUp() throws Exception {
        // Configure the Oracle Database container with the TxEventQ test user.
        oracleContainer.start();
        oracleContainer.copyFileToContainer(MountableFile.forClasspathResource("init.sql"), "/tmp/init.sql");
        oracleContainer.execInContainer("sqlplus", "sys / as sysdba", "@/tmp/init.sql");

        // Configure a datasource for the Oracle Database container.
        // The datasource is used to demonstrate TxEventQ table duality.
        dataSource = new OracleDataSource();
        dataSource.setUser(testUser);
        dataSource.setPassword(testPassword);
        dataSource.setURL(oracleContainer.getJdbcUrl());
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("bootstrap.servers", () -> "localhost:" + oracleContainer.getOraclePort());
        registry.add("ojdbc.path", () -> new File("src/main/resources").getAbsolutePath());
        registry.add("producer.stream.file", () -> new File("src/test/resources/weather_sensor_data.txt").getAbsolutePath());
    }

    @Autowired
    OKafkaComponent okafkaComponent;

    @Test
    @Timeout(value = 10)
    void okafkaSample() throws Exception {
        // Wait for the consumer and producer to complete.
        okafkaComponent.await();

        // Verify the OKafka messages hit the database, using SQL!
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("""
                    select * from OKAFKA_SAMPLE
                    fetch first 1 row only
                    """);
            // Verify that data is present in the OKAFKA_SAMPLE topic table
            assertThat(resultSet.next()).isTrue();
            ResultSetMetaData metaData = resultSet.getMetaData();
            System.out.println("#### OKAFKA_SAMPLE Columns: ####");
            metaData.getColumnCount();
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                System.out.println(metaData.getColumnName(i + 1));
            }
        }
    }
}
