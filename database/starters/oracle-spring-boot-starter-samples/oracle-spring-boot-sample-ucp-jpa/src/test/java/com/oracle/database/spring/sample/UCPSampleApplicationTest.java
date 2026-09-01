// Copyright (c) 2024, 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.sample;

import java.time.Duration;
import java.util.List;

import com.oracle.database.spring.testcontainers.OracleContainer;
import javax.sql.DataSource;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Sql("/init.sql") // Initialize the student table
public class UCPSampleApplicationTest {

    /**
     * OracleContainer creates an Oracle AI Database Free container in a JUnit context.
     */
    @Container
    static OracleContainer oracleContainer = new OracleContainer()
            .withStartupTimeout(Duration.ofMinutes(2))
            .withUsername("testuser")
            .withPassword("testpwd");

    /**
     * Dynamically configure Spring Boot properties to use the Testcontainers database.
     */
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("JDBC_URL", oracleContainer::getJdbcUrl);
        registry.add("USERNAME", oracleContainer::getUsername);
        registry.add("PASSWORD", oracleContainer::getPassword);
    }

    @Autowired
    StudentController studentController;

    @Autowired
    MeterRegistry meterRegistry;

    @Autowired
    DataSource dataSource;

    @Test
    void ucpSampleApp() throws Exception {

        // Create a new student
        Student student1 = new Student(
                "Alice",
                "Smith",
                "alice.smith@myuni.edu",
                "Computer Science",
                60,
                3.83
        );
        Student s1 = studentController.createStudent(student1).getBody();
        assertThat(s1).isNotNull();
        assertThat(s1.getFirstName()).isEqualTo("Alice");

        // Assert student created by querying list/get methods
        List<Student> students = studentController.listStudents().getBody();
        assertThat(students).hasSize(1);
        assertThat(students.get(0).getFirstName()).isEqualTo("Alice");

        Student s2 = studentController.getStudent(s1.getId()).getBody();
        assertThat(s2).isNotNull();
        assertThat(s2.getFirstName()).isEqualTo(s1.getFirstName());

        // Delete student and assert student is no longer found
        studentController.deleteStudent(s1.getId());
        ResponseEntity<Student> re = studentController.getStudent(s1.getId());
        assertThat(re.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));

        // Verify UCP metrics are emitted
        Gauge idleConnections = meterRegistry.get("db.client.connection.count")
                .tag("db.client.connection.pool.name", "UCPSampleApplication")
                .tag("db.client.connection.state", "idle")
                .gauge();
        Gauge usedConnections = meterRegistry.get("db.client.connection.count")
                .tag("db.client.connection.pool.name", "UCPSampleApplication")
                .tag("db.client.connection.state", "used")
                .gauge();
        FunctionCounter borrowedConnections = meterRegistry.get("ucp.connections.borrowed")
                .tag("pool", "UCPSampleApplication")
                .functionCounter();
        FunctionCounter returnedConnections = meterRegistry.get("ucp.connections.returned")
                .tag("pool", "UCPSampleApplication")
                .functionCounter();
        Gauge maxConnections = meterRegistry.get("db.client.connection.max")
                .tag("db.client.connection.pool.name", "UCPSampleApplication")
                .gauge();

        assertThat(idleConnections.value()).isGreaterThan(0);
        try (var ignored = dataSource.getConnection()) {
            assertThat(usedConnections.value()).isGreaterThan(0);
        }
        assertThat(borrowedConnections.count()).isGreaterThan(0);
        assertThat(returnedConnections.count()).isGreaterThan(0);
        assertThat(maxConnections.value()).isEqualTo(30.0);
    }
}
