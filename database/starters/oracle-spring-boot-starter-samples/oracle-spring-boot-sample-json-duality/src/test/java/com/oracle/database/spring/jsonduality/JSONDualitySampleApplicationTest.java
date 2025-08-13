// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonduality;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Sql("/init.sql") // Initialize the tables and duality views
public class JSONDualitySampleApplicationTest {
    /**
     * The Testcontainers Oracle Free module let's us create an Oracle database container in a junit context.
     */
    @Container
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.9-slim-faststart")
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
    StudentService studentService;

    @Autowired
    CourseService courseService;

    @Test
    void jsonDualityViewsSampleApplication() {
        System.out.println("#### Querying Courses By Name:");
        // fetch courses
        List<Course> courseByName = courseService.getCourseByName("Introduction to Computer Science");
        assertThat(courseByName).hasSize(1);
        Course introToCS = courseByName.get(0);
        System.out.println("#### Intro to Computer Science:\n" + introToCS);
        courseByName = courseService.getCourseByName("Data Structures and Algorithms");
        assertThat(courseByName).hasSize(1);
        Course dsAndAlgo = courseByName.get(0);
        System.out.println("\n#### Data Structures and Algorithms:\n" + dsAndAlgo);

        System.out.println("\n\n\n#### Enrolling Student in CS101");
        // Enroll existing student in a new course
        Student aliceSmith = getStudent("Alice", "Smith");
        Enrollment introToCSEnrollment = new Enrollment();
        introToCSEnrollment.setCourse(introToCS);
        List<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(introToCSEnrollment);
        aliceSmith.setEnrollments(enrollments);
        studentService.updateStudent(aliceSmith);

        // Verify enrollment is added
        aliceSmith = getStudent("Alice", "Smith");
        List<Enrollment> asEnrollments = aliceSmith.getEnrollments();
        assertThat(asEnrollments).hasSize(1);
        assertThat(asEnrollments.get(0).getCourse().getName())
                .isEqualTo(introToCS.getName());
        assertThat(asEnrollments.get(0).getCourse().getLecture_hall().getName())
                .isEqualTo("Hoffman Hall");
        System.out.println("#### Enrollment created:\n" + aliceSmith);


        System.out.println("\n\n\n#### Creating new student with two enrollments");
        // Create a new student with two enrollments
        Student bobSwanson = new Student();
        bobSwanson.setFirst_name("Robert");
        bobSwanson.setLast_name("Swanson");
        bobSwanson.setMajor("Electrical Engineering");
        bobSwanson.setEmail("bob.swanson@xyz.edu");
        bobSwanson.setCredits(44);
        bobSwanson.setGpa(3.33);

        Enrollment dsAndAlgoEnrollment = new Enrollment();
        dsAndAlgoEnrollment.setCourse(dsAndAlgo);
        bobSwanson.setEnrollments(List.of(introToCSEnrollment, dsAndAlgoEnrollment));
        studentService.addStudent(bobSwanson);

        // Verify student created with enrollments
        bobSwanson = getStudent("Robert", "Swanson");
        assertThat(bobSwanson.getEnrollments()).hasSize(2);
        System.out.println("#### Student created:\n" + bobSwanson);
    }

    private Student getStudent(String firstName, String lastName) {
        List<Student> studentByName = studentService.getStudentByName(firstName, lastName);
        assertThat(studentByName).hasSize(1);
        return studentByName.get(0);
    }


}
