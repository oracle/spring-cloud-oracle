// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.util.List;
import java.util.UUID;

import com.oracle.database.spring.jsonevents.model.Sensor;
import com.oracle.database.spring.jsonevents.model.SensorEvent;
import com.oracle.database.spring.jsonevents.model.Station;
import org.springframework.stereotype.Service;

/**
 * Convert SensorEvents into Sensor lists
 */
@Service
public class SensorEventParser {
    public List<Sensor> parse(SensorEvent sensorEvent) {
        return sensorEvent.getData().stream()
                .map(e -> e.split(","))
                .filter(s -> s.length == 4)
                .map(s -> {
                    Station st  = new Station();
                    st.set_id(s[0]);

                    Sensor sd = new Sensor();
                    sd.set_id(UUID.randomUUID().toString());
                    sd.setStation(st);
                    sd.setTemperature(Double.parseDouble(s[1]));
                    sd.setRelativeHumidity(Double.parseDouble(s[2]));
                    sd.setUvIndex(Double.parseDouble(s[3]));
                    return sd;
                }).toList();
    }
}
