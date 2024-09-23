// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.util.ArrayList;
import java.util.List;

import com.oracle.database.spring.jsonevents.model.SensorData;
import com.oracle.database.spring.jsonevents.model.SensorEvent;
import org.springframework.stereotype.Service;

@Service
public class SensorEventParser {
    public List<SensorData> parse(SensorEvent sensorEvent) {
        return new ArrayList<>();
    }
}
