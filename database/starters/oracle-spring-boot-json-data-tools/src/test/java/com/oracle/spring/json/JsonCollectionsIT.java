// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json;

import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import com.oracle.spring.json.jsonb.JSONB;
import com.oracle.spring.json.jsonb.JSONBRowMapper;
import com.oracle.spring.json.test.Student;
import com.oracle.spring.json.test.StudentDetails;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JsonCollectionsAutoConfiguration.class)
@Testcontainers
public class JsonCollectionsIT {
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.9-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withUsername("testuser")
            .withPassword("testpwd");

    static DataSource dataSource;
    static JdbcTemplate jdbcTemplate;

    @Autowired
    JSONB jsonb;

    Student student1 = new Student(Student.newId(), "Bob", new StudentDetails(
            "Computer Science",
            3.33,
            64
    ));

    @BeforeAll
    static void setUp() throws Exception {
        oracleContainer.start();
        PoolDataSource ds = PoolDataSourceFactory.getPoolDataSource();
        ds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        ds.setConnectionPoolName(UUID.randomUUID().toString());
        ds.setURL(oracleContainer.getJdbcUrl());
        ds.setUser(oracleContainer.getUsername());
        ds.setPassword(oracleContainer.getPassword());
        dataSource = ds;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void contextLoads() {}

    @Test
    void jsonbRowMapping() {
        jdbcTemplate.execute("""
            create table student (
                data json
            )
            """);
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("insert into student (data) values (?)");
            byte[] oson = jsonb.toOSON(student1);
            ps.setObject(1, oson, OracleTypes.JSON);
            return ps;
        });
        RowMapper<Student> rowMapper = new JSONBRowMapper<>(jsonb, Student.class, 1);
        List<Student> students = jdbcTemplate.query("select * from student", rowMapper);
        assertThat(students).hasSize(1);
        assertThat(students.get(0)).isEqualTo(student1);
    }
}
