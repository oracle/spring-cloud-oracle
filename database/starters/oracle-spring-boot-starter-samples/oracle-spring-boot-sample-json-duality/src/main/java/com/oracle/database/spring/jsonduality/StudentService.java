// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonduality;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;

import com.oracle.spring.json.jsonb.JSONB;
import com.oracle.spring.json.jsonb.JSONBRowMapper;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StudentService {
    private static final String queryStudents = """
            select * from students_dv
            """;
    private static final String insertStudent = """
            insert into students_dv (data) values (?)
            """;
    private static final String updateStudent = """
            update students_dv v set data = ?
            where v.data."_id" = ?
            """;
    private static final String byName = """
            select * from students_dv v
            where v.data.first_name = ?
            and v.data.last_name = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final JSONB jsonb;
    private final RowMapper<Student> rowMapper;


    public StudentService(JdbcTemplate jdbcTemplate, JSONB jsonb) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonb = jsonb;
        rowMapper = new JSONBRowMapper<>(this.jsonb, Student.class);
    }

    public String addStudent(Student student) {
        if (!StringUtils.hasText(student.get_id())) {
            student.set_id(UUID.randomUUID().toString());
        }
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertStudent);
            byte[] oson = jsonb.toOSON(student);
            ps.setObject(1, oson, OracleTypes.JSON);
            return ps;
        });
        return student.get_id();
    }

    public void updateStudent(Student student) {
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(updateStudent);
            byte[] oson = jsonb.toOSON(student);
            ps.setObject(1, oson, OracleTypes.JSON);
            ps.setString(2, student.get_id());
            return ps;
        });
    }

    public List<Student> getStudentByName(String firstName, String lastName) {
        RowMapper<Student> rowMapper = new JSONBRowMapper<>(jsonb, Student.class);
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(byName);
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            return ps;
        }, rowMapper);
    }

    public List<Student> getStudents() {
        return jdbcTemplate.query(queryStudents, rowMapper);
    }
}
