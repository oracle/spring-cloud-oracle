// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.kafka;

import com.oracle.spring.json.jsonb.JSONB;

public class OSONKafkaSerializationFactory {
    private final JSONB jsonb;

    public OSONKafkaSerializationFactory(JSONB jsonb) {
        this.jsonb = jsonb;
    }

    public <T> OSONDeserializer<T> createDeserializer(Class<T> clazz) {
        return new OSONDeserializer<>(jsonb, clazz);
    }

    public <T> OSONSerializer<T> createSerializer() {
        return new OSONSerializer<>(jsonb);
    }
}
