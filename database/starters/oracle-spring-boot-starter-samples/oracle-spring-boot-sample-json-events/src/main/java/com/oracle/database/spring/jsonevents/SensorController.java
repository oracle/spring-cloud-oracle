// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.util.List;

import com.oracle.database.spring.jsonevents.model.SensorData;
import com.oracle.database.spring.jsonevents.model.SensorEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class SensorController {
    private final SensorEventProducer sensorEventProducer;
    private final SensorDataService sensorDataService;

    public SensorController(SensorEventProducer sensorEventProducer, SensorDataService sensorDataService) {
        this.sensorEventProducer = sensorEventProducer;
        this.sensorDataService = sensorDataService;
    }

    @PostMapping
    public ResponseEntity<?> produce(@RequestBody SensorEvent event) {
        sensorEventProducer.send(event);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/station/{stationId}")
    ResponseEntity<?> getEvents(@PathVariable String stationId) {
        List<SensorData> sensorData = sensorDataService.byStationId(stationId);
        return ResponseEntity.ok(sensorData);
    }
}
