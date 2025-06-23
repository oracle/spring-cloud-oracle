// Copyright (c) 2024, 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.test;

import java.util.Objects;
import java.util.UUID;

public class Student {
    String id;
    String name;
    StudentDetails details;

    public static String newId() {
        return UUID.randomUUID().toString();
    }

    public Student() {}

    public Student(String id, String name, StudentDetails details) {
        this.id = id;
        this.name = name;
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StudentDetails getDetails() {
        return details;
    }

    public void setDetails(StudentDetails details) {
        this.details = details;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Student student)) return false;

        return Objects.equals(getId(), student.getId()) && Objects.equals(getName(), student.getName()) && Objects.equals(getDetails(), student.getDetails());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getId());
        result = 31 * result + Objects.hashCode(getName());
        result = 31 * result + Objects.hashCode(getDetails());
        return result;
    }
}
