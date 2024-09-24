package com.oracle.database.spring.jsonevents;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.oracle.database.spring.jsonevents.model.Sensor;
import com.oracle.database.spring.jsonevents.model.Station;
import org.springframework.stereotype.Service;

@Service
public class SensorEnricher {
    private final StationService stationService;

    public SensorEnricher(StationService stationService) {
        this.stationService = stationService;
    }

    public Sensor enrich(Sensor sensor) {
        Objects.requireNonNull(sensor, "sensor cannot be null");
        List<Station> query = stationService.byId(sensor.getStation().get_id());
        if (query.isEmpty()) {
            throw new IllegalStateException("No weather station found for id " + sensor.getStation().get_id());
        }
        sensor.setStation(query.get(0));
        return sensor;
    }
}
