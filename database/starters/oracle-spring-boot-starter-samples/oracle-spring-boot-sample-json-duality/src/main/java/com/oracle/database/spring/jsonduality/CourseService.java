// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonduality;

import java.sql.PreparedStatement;
import java.util.List;

import com.oracle.spring.json.jsonb.JSONB;
import com.oracle.spring.json.jsonb.JSONBRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class CourseService {
    private static final String byName = """
            select * from courses_dv v
            where v.data.name = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final JSONB jsonb;
    private final RowMapper<Course> rowMapper;

    public CourseService(JdbcTemplate jdbcTemplate, JSONB jsonb) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonb = jsonb;
        rowMapper = new JSONBRowMapper<>(jsonb, Course.class);
    }

    public List<Course> getCourseByName(String name) {
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(byName);
            ps.setString(1, name);
            return ps;
        }, rowMapper);
    }
}
