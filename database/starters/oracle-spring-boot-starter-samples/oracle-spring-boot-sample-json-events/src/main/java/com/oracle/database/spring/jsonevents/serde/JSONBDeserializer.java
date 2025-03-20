// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents.serde;

import java.nio.ByteBuffer;

import com.oracle.spring.json.jsonb.JSONB;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * The JSONBDeserializer converts JSONB byte arrays to java objects.
 * @param <T> deserialization type
 */
public class JSONBDeserializer<T> implements Deserializer<T> {
    private final JSONB jsonb;
    private final Class<T> clazz;

    public JSONBDeserializer(JSONB jsonb, Class<T> clazz) {
        this.jsonb = jsonb;
        this.clazz = clazz;
    }

    @Override
    public T deserialize(String s, byte[] bytes) {
        return jsonb.fromOSON(ByteBuffer.wrap(bytes), clazz);
    }
}
