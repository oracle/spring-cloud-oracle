// Copyright (c) 2024, 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.sql.PreparedStatement;
import java.util.List;

import com.oracle.database.spring.jsonevents.model.Station;
import com.oracle.spring.json.jsonb.JSONB;
import com.oracle.spring.json.jsonb.JSONBRowMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
@SuppressFBWarnings(value = {"EI2", "OBL"},
        justification = "JdbcTemplate owns PreparedStatements returned by its PreparedStatementCreator callback.")
public class StationService {
    private static final String stationById = """
            select * from station_dv v
            where v.data."_id" = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Station> rowMapper;

    public StationService(JdbcTemplate jdbcTemplate, JSONB jsonb) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = new JSONBRowMapper<>(jsonb, Station.class);
    }


    public List<Station> byId(String stationId) {
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(stationById);
            ps.setString(1, stationId);
            return ps;
        }, rowMapper);
    }
}
