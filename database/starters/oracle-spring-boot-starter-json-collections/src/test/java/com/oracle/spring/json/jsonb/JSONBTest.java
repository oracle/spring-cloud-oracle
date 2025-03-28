// Copyright (c) 2024, 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.jsonb;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.oracle.spring.json.test.Student;
import com.oracle.spring.json.test.StudentDetails;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.stream.JsonParser;
import oracle.sql.json.OracleJsonFactory;
import org.eclipse.yasson.YassonJsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JSONBTest {

    private JSONB jsonb = JSONB.createDefault();
    private Student s = new Student(Student.newId(), "Alice", new StudentDetails(
            "Mathematics",
            3.77,
            44
    ));


    @Test
    void toOSON() {
        byte[] oson = jsonb.toOSON(s);
        assertThat(oson).hasSizeGreaterThan(1);
    }

    @Test
    void parserToObject() {
        JsonParser parser = jsonb.toJsonParser(s);
        Student student = jsonb.fromOSON(parser, Student.class);
        assertThat(student).isNotNull();
        assertThat(student).isEqualTo(s);
    }

    @Test
    void inputStreamToObject() {
        byte[] oson = jsonb.toOSON(s);
        InputStream is = new ByteArrayInputStream(oson);

        Student student = jsonb.fromOSON(is, Student.class);
        assertThat(student).isNotNull();
        assertThat(student).isEqualTo(s);
    }

    @Test
    void byteBufferToOjbect() {
        byte[] oson = jsonb.toOSON(s);
        ByteBuffer buf = ByteBuffer.wrap(oson);

        Student student = jsonb.fromOSON(buf, Student.class);
        assertThat(student).isNotNull();
        assertThat(student).isEqualTo(s);
    }
}
