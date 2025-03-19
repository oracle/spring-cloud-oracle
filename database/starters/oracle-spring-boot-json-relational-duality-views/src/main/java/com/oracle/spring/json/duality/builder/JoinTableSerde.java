// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.builder;

import java.lang.reflect.Type;

import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;

public class JoinTableSerde implements JsonbSerializer<Class<?>>, JsonbDeserializer<Class<?>> {
    @Override
    public void serialize(Class<?> aClass, JsonGenerator jsonGenerator, SerializationContext serializationContext) {

    }

    @Override
    public Class<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, Type type) {
        return null;
    }
}
