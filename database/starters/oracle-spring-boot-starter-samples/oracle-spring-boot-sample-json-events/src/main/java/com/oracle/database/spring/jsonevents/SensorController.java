// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.util.List;

import com.oracle.database.spring.jsonevents.model.Sensor;
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
    private final SensorService sensorService;

    public SensorController(SensorEventProducer sensorEventProducer, SensorService sensorService) {
        this.sensorEventProducer = sensorEventProducer;
        this.sensorService = sensorService;
    }

    @PostMapping
    public ResponseEntity<?> produce(@RequestBody SensorEvent event) {
        sensorEventProducer.send(event);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/station/{stationId}")
    ResponseEntity<List<Sensor>> getEvents(@PathVariable String stationId) {
        List<Sensor> sensors = sensorService.byStationId(stationId);
        return ResponseEntity.ok(sensors);
    }
}
