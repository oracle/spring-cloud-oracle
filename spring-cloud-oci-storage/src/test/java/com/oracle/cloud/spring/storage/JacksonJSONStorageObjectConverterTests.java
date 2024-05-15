/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.mock;

class JacksonJSONStorageObjectConverterTests {

    String expectedValue = "{\"name\":\"testName\"}";

    @Test
    void testStorageObjectConverterWithNullObjectMapper() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new JacksonJSONStorageObjectConverter(null);
        });
        assertEquals(JacksonJSONStorageObjectConverter.ERROR_OBJECTMAPPER_REQUIRED, exception.getMessage());
    }

    @Test
    void testWriteStorageObject() {
        Person person = new Person();
        person.setName("testName");
        byte[] out = new JacksonJSONStorageObjectConverter(new ObjectMapper()).write(person);
        String actualValue = new String(out);
        assertEquals(expectedValue, actualValue);

        assertThrows(StorageException.class, () -> {
            new JacksonJSONStorageObjectConverter(new ObjectMapper()).write(mock(Object.class));
        });
    }

    @Test
    void testReadStorageObject() throws Exception {
        JacksonJSONStorageObjectConverter converter = new JacksonJSONStorageObjectConverter(new ObjectMapper());
        Person person = converter.read(new ByteArrayInputStream(expectedValue.getBytes()), Person.class);
        assertEquals("testName", person.getName());
        assertEquals("application/json", converter.contentType());
        assertThrows(StorageException.class, () -> {
            converter.read(new ByteArrayInputStream(expectedValue.getBytes()), String.class);
        });
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
