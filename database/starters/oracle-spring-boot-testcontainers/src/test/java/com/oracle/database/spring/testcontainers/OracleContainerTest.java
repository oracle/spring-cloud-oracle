// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import oracle.jdbc.pool.OracleDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class OracleContainerTest {

    @Container
    static final OracleContainer ORACLE_CONTAINER =
            new OracleContainer()
                    .withAppUserRoles("connect", "db_developer_role", "CONNECT")
                    .withInitScript("students.sql");

    static OracleDataSource dataSource;

    @BeforeAll
    static void setUp() throws SQLException {
        dataSource = new OracleDataSource();
        dataSource.setURL(ORACLE_CONTAINER.getJdbcUrl());
        dataSource.setUser(ORACLE_CONTAINER.getUsername());
        dataSource.setPassword(ORACLE_CONTAINER.getPassword());
    }

    @Test
    void getConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "select first_name from students where email = 'alice.smith@example.edu'")) {
            assertTrue(resultSet.next(), "Expected the initialized application schema to contain a student");
            assertEquals("Alice", resultSet.getString("first_name"));
        }
    }

    @Test
    void grantsConfiguredApplicationUserRoles() throws SQLException {
        Set<String> roles = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "select role from session_roles where role in ('CONNECT', 'DB_DEVELOPER_ROLE')")) {
            while (resultSet.next()) {
                roles.add(resultSet.getString("role"));
            }
        }

        assertEquals(Set.of("CONNECT", "DB_DEVELOPER_ROLE"), roles);
    }
}
