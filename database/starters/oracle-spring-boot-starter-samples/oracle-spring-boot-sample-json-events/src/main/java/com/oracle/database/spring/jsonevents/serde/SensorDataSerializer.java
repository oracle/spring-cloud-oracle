package com.oracle.database.spring.jsonevents.serde;

import com.oracle.database.spring.jsonevents.model.SensorData;
import com.oracle.spring.json.jsonb.JSONB;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.stereotype.Component;

/**
 * The SensorDataSerializer uses the Spring JSON Collections JSONB utility class
 * to serialize SensorData objects to OSON in JSONB format (Binary JSON).
 */
@Component
public class SensorDataSerializer implements Serializer<SensorData> {
    private final JSONB jsonb;

    public SensorDataSerializer(JSONB jsonb) {
        this.jsonb = jsonb;
    }

    @Override
    public byte[] serialize(String s, SensorData sensorData) {
        return jsonb.toOSON(sensorData);
    }
}
