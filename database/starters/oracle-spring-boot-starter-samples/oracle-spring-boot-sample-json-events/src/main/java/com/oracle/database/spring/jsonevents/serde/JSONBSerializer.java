// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents.serde;

import com.oracle.spring.json.jsonb.JSONB;
import org.apache.kafka.common.serialization.Serializer;

/**
 * The JSONBSerializer converts java objects to a JSONB byte array.
 * @param <T> serialization type.
 */
public class JSONBSerializer<T> implements Serializer<T> {
    private final JSONB jsonb;

    public JSONBSerializer(JSONB jsonb) {
        this.jsonb = jsonb;
    }

    @Override
    public byte[] serialize(String s, T obj) {
        return jsonb.toOSON(obj);
    }
}
