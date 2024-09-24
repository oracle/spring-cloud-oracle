// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.sql.PreparedStatement;
import java.util.List;

import com.oracle.database.spring.jsonevents.model.Sensor;
import com.oracle.spring.json.jsonb.JSONB;
import com.oracle.spring.json.jsonb.JSONBRowMapper;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SensorService {
    private static final String insertSensorData = """
            insert into weather_sensor_dv (data) values (?)
            """;

    private static final String byStationId = """
            select * from weather_sensor_dv v
            where v.sensor_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final JSONB jsonb;
    private final RowMapper<Sensor> rowMapper;

    public SensorService(JdbcTemplate jdbcTemplate, JSONB jsonb) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonb = jsonb;
        this.rowMapper = new JSONBRowMapper(jsonb, Sensor.class);
    }

    public void save(Sensor sensor) {
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertSensorData);
            byte[] oson = jsonb.toOSON(sensor);
            ps.setObject(1, oson, OracleTypes.JSON);
            return ps;
        });
    }

    public List<Sensor> byStationId(String stationId) {
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(byStationId);
            ps.setString(1, stationId);
            return ps;
        }, rowMapper);
    }
}
