// Copyright (c) 2024, 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.jsonb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import jakarta.json.bind.JsonbBuilder;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import oracle.sql.json.OracleJsonFactory;
import org.eclipse.yasson.YassonJsonb;

/**
 * The JSONB bean provides utility methods to convert Java objects to and from OSON.
 * You may inject this bean into your application, or use the createDefault factory
 * method to create a new instance.
 */
public class JSONB {
    private final OracleJsonFactory oracleJsonFactory;
    private final YassonJsonb jsonb;

    /**
     * Create a new JSONB instance.
     * @return Default JSONB instance.
     */
    public static JSONB createDefault() {
        return new JSONB(new OracleJsonFactory(), (YassonJsonb) JsonbBuilder.create());
    }

    public JSONB(OracleJsonFactory oracleJsonFactory, YassonJsonb jsonb) {
        this.oracleJsonFactory = oracleJsonFactory;
        this.jsonb = jsonb;
    }

    /**
     * Converts a Java object to an OSON byte array.
     * @param o Java object to convert to OSON.
     * @return OSON byte array.
     */
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

    /**
     * Creates an OSON JsonParser from a Java object.
     * @param o Java object to create a JsonParser from.
     * @return JsonParser for generating OSON.
     */
    public JsonParser toJsonParser(Object o) {
        byte[] oson = toOSON(o);
        ByteBuffer buf = ByteBuffer.wrap(oson);
        return oracleJsonFactory.createJsonBinaryParser(buf).wrap(JsonParser.class);
    }

    /**
     * Convert an OSON byte array to a Java object of type T.
     * @param oson OSON byte array.
     * @param clazz Java class to convert OSON to.
     * @param <T> Type parameter for the Java conversion class.
     * @return Converted Java object of type T.
     * @throws IOException When OSON parsing fails.
     */
    public <T> T fromOSON(byte[] oson, Class<T> clazz) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(oson)) {
            return fromOSON(inputStream, clazz);
        }
    }

    /**
     * Create a Java object of type T from an OSON JsonParser.
     * @param parser OSON JsonParser.
     * @param clazz Java object to create.
     * @param <T> Type parameter for the Java object.
     * @return Converted Java object of type T.
     */
    public <T> T fromOSON(JsonParser parser, Class<T> clazz) {
        return jsonb.fromJson(parser, clazz);
    }

    /**
     * Create a Java object from an OSON InputStream.
     * @param inputStream OSON InputStream.
     * @param clazz Java object to create.
     * @param <T> Type parameter for the Java object.
     * @return Converted Java object of type T.
     */
    public <T> T fromOSON(InputStream inputStream, Class<T> clazz) {
        JsonParser jsonParser = oracleJsonFactory.createJsonBinaryParser(inputStream).wrap(JsonParser.class);
        return jsonb.fromJson(jsonParser, clazz);
    }

    /** Create a Java Object from an OSON ByteBuffer.
     * @param byteBuffer OSON ByteBuffer.
     * @param clazz Java object to create.
     * @param <T> Type parameter for the Java object.
     * @return Converted Java object of type T.
     */
    public <T> T fromOSON(ByteBuffer byteBuffer, Class<T> clazz) {
        JsonParser jsonParser = oracleJsonFactory.createJsonBinaryParser(byteBuffer).wrap(JsonParser.class);
        return jsonb.fromJson(jsonParser, clazz);
    }
}
