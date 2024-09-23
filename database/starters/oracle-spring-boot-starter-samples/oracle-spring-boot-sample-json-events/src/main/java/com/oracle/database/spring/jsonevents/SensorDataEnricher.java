package com.oracle.database.spring.jsonevents;

import java.util.List;
import java.util.Objects;

import com.oracle.database.spring.jsonevents.model.SensorData;
import com.oracle.database.spring.jsonevents.model.WeatherStation;
import org.springframework.stereotype.Service;

@Service
public class SensorDataEnricher {
    private final WeatherStationService weatherStationService;

    public SensorDataEnricher(WeatherStationService weatherStationService) {
        this.weatherStationService = weatherStationService;
    }

    public SensorData enrich(SensorData sensorData) {
        Objects.requireNonNull(sensorData, "sensorData cannot be null");
        if (sensorData.getStation() != null) {
            return sensorData;
        }
        List<WeatherStation> query = weatherStationService.byId(sensorData.getStationId());
        if (query.isEmpty()) {
            throw new IllegalStateException("No weather station found for id " + sensorData.getStationId());
        }
        sensorData.setStation(query.get(0));
        return sensorData;
    }
}
