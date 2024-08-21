// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.sample;

import java.time.Duration;
import java.util.List;

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
import org.testcontainers.oracle.OracleContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Sql("/init.sql") // Initialize the student table
public class UCPSampleApplicationTest {

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

    @Autowired
    StudentController studentController;

    @Test
    void ucpSampleApp() {

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
    }
}
