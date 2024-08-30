// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.json;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import com.oracle.database.spring.json.jsonb.JSONB;
import com.oracle.database.spring.json.jsonb.JSONBRowMapper;
import com.oracle.database.spring.json.jsonb.SODA;
import com.oracle.database.spring.json.test.Student;
import com.oracle.database.spring.json.test.StudentDetails;
import oracle.jdbc.OracleTypes;
import oracle.soda.OracleCollection;
import oracle.soda.OracleDatabase;
import oracle.soda.OracleDocument;
import oracle.soda.rdbms.OracleRDBMSClient;
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
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.5-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withUsername("testuser")
            .withPassword(("testpwd"));

    static DataSource dataSource;
    static JdbcTemplate jdbcTemplate;

    @Autowired
    JSONB jsonb;
    @Autowired
    SODA soda;
    @Autowired
    OracleRDBMSClient client;

    Student student1 = new Student(Student.newId(), "Bob", new StudentDetails(
            "Computer Science",
            3.33,
            64
    ));
    Student student2 = new Student(Student.newId(), "Alice", new StudentDetails(
            "Mathematics",
            3.8,
            80
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

    @Test
    void sodaMapping() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            OracleDatabase db = client.getDatabase(conn);
            db.admin().createCollection("student_soda");
            OracleCollection col = db.openCollection("student_soda");

            OracleDocument document1 = soda.toDocument(db, student1);
            OracleDocument document2 = soda.toDocument(db, student2);

            col.insert(document1);
            col.insert(document2);

            OracleDocument found = col.find().filter("{\"name\":\"Alice\"}")
                    .getOne();

            Student student = soda.fromDocument(found, Student.class);
            assertThat(student).isEqualTo(student2);
        }
    }
}
