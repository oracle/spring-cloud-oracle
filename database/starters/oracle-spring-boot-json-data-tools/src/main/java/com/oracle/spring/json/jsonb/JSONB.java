// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.jsonb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import oracle.sql.json.OracleJsonFactory;
import org.eclipse.yasson.YassonJsonb;

public class JSONB {
    private final OracleJsonFactory oracleJsonFactory;
    private final YassonJsonb jsonb;

    public JSONB(OracleJsonFactory oracleJsonFactory, YassonJsonb jsonb) {
        this.oracleJsonFactory = oracleJsonFactory;
        this.jsonb = jsonb;
    }

    public byte[] toOSON(Object o) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())  {
            JsonGenerator gen = oracleJsonFactory.createJsonBinaryGenerator(outputStream).wrap(JsonGenerator.class);
            jsonb.toJson(o, gen);
            gen.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonParser toJsonParser(Object o) {
        byte[] oson = toOSON(o);
        ByteBuffer buf = ByteBuffer.wrap(oson);
        return oracleJsonFactory.createJsonBinaryParser(buf).wrap(JsonParser.class);
    }

    public <T> T fromOSON(JsonParser parser, Class<T> clazz) {
        return jsonb.fromJson(parser, clazz);
    }

    public <T> T fromOSON(InputStream inputStream, Class<T> clazz) {
        JsonParser jsonParser = oracleJsonFactory.createJsonBinaryParser(inputStream).wrap(JsonParser.class);
        return jsonb.fromJson(jsonParser, clazz);
    }

    public <T> T fromOSON(ByteBuffer byteBuffer, Class<T> clazz) {
        JsonParser jsonParser = oracleJsonFactory.createJsonBinaryParser(byteBuffer).wrap(JsonParser.class);
        return jsonb.fromJson(jsonParser, clazz);
    }
}
