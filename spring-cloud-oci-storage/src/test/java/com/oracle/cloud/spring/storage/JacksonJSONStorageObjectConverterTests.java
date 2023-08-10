/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class JacksonJSONStorageObjectConverterTests {

    String expectedValue = "{\"name\":\"testName\"}";

    @Test
    public void testStorageObjectConverterWithNullObjectMapper() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new JacksonJSONStorageObjectConverter(null);
        });
        assertTrue(JacksonJSONStorageObjectConverter.ERROR_OBJECTMAPPER_REQUIRED.equals(exception.getMessage()));
    }

    @Test
    public void testWriteStorageObject() {
        Person person = new Person();
        person.setName("testName");
        byte[] out = new JacksonJSONStorageObjectConverter(new ObjectMapper()).write(person);
        String actualValue = new String(out);
        assertTrue(expectedValue.equals(actualValue));
    }

    @Test
    public void testReadStorageObject() throws Exception {
        Person person = new JacksonJSONStorageObjectConverter(new ObjectMapper()).read(new ByteArrayInputStream(expectedValue.getBytes()), Person.class);
        assertTrue("testName".equals(person.getName()));
    }

    private JSONObject getJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", "testName");
        return json;
    }
}

class Person {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
