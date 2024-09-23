package com.oracle.database.spring.jsonevents;

import java.sql.PreparedStatement;
import java.util.List;

import com.oracle.database.spring.jsonevents.model.WeatherStation;
import com.oracle.spring.json.jsonb.JSONB;
import com.oracle.spring.json.jsonb.JSONBRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class WeatherStationService {
    private static final String stationById = """
            select * from weather_station_dv v
            where where v.data."_id" = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<WeatherStation> rowMapper;

    public WeatherStationService(JdbcTemplate jdbcTemplate, JSONB jsonb) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = new JSONBRowMapper(jsonb, WeatherStation.class);
    }


    public List<WeatherStation> byId(String stationId) {
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(stationById);
            ps.setString(1, stationId);
            return ps;
        }, rowMapper);
    }
}
