package com.oracle.database.spring.jsonevents.serde;

import java.io.IOException;
import java.io.InputStream;

import com.oracle.database.spring.jsonevents.model.SensorData;
import com.oracle.spring.json.jsonb.JSONB;
import org.springframework.core.serializer.Deserializer;
import org.springframework.stereotype.Component;

@Component
public class SensorDataDeserializer implements Deserializer<SensorData> {
    private final JSONB jsonb;

    public SensorDataDeserializer(JSONB jsonb) {
        this.jsonb = jsonb;
    }

    @Override
    public SensorData deserialize(InputStream inputStream) throws IOException {
        return jsonb.fromOSON(inputStream, SensorData.class);
    }
}
