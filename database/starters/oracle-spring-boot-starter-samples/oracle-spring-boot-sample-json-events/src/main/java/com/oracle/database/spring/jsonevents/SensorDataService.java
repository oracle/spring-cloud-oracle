package com.oracle.database.spring.jsonevents;

import java.sql.PreparedStatement;
import java.util.List;

import com.oracle.database.spring.jsonevents.model.SensorData;
import com.oracle.spring.json.jsonb.JSONB;
import com.oracle.spring.json.jsonb.JSONBRowMapper;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SensorDataService {
    private static final String insertSensorData = """
            insert into sensor_data_dv (data) values (?)
            """;

    private static final String byStationId = """
            select * from sensor_data_dv v
            where v.sensor_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final JSONB jsonb;
    private final RowMapper<SensorData> rowMapper;

    public SensorDataService(JdbcTemplate jdbcTemplate, JSONB jsonb) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonb = jsonb;
        this.rowMapper = new JSONBRowMapper(jsonb, SensorData.class);
    }

    public void save(SensorData sensorData) {
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertSensorData);
            byte[] oson = jsonb.toOSON(sensorData);
            ps.setObject(1, oson, OracleTypes.JSON);
            return ps;
        });
    }

    public List<SensorData> byStationId(String stationId) {
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(byStationId);
            ps.setString(1, stationId);
            return ps;
        }, rowMapper);
    }
}
