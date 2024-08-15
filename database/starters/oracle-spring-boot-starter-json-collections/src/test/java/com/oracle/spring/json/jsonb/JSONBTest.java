// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.jsonb;

import com.oracle.spring.json.test.Student;
import com.oracle.spring.json.test.StudentDetails;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.stream.JsonParser;
import oracle.sql.json.OracleJsonFactory;
import org.eclipse.yasson.YassonJsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JSONBTest {

    private JSONB jsonb = new JSONB(new OracleJsonFactory(), (YassonJsonb) JsonbBuilder.create());
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
}
