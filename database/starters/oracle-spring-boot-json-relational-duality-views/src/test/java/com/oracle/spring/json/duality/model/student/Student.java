// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.model.student;

import java.util.Objects;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import com.oracle.spring.json.duality.annotation.AccessMode;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static com.oracle.spring.json.duality.builder.Annotations._ID_FIELD;

@Entity
@Table(name = "STUDENT")
@JsonRelationalDualityView(
        accessMode = @AccessMode(
                insert = true,
                update = true,
                delete = true
        )
)
public class Student {
    @JsonbProperty(_ID_FIELD)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    private String email;
    private String major;
    private double credits;
    private double gpa;

    public Student() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Student student)) return false;

        return Double.compare(getCredits(), student.getCredits()) == 0 && Double.compare(getGpa(), student.getGpa()) == 0 && Objects.equals(getId(), student.getId()) && Objects.equals(getFirstName(), student.getFirstName()) && Objects.equals(getLastName(), student.getLastName()) && Objects.equals(getEmail(), student.getEmail()) && Objects.equals(getMajor(), student.getMajor());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getId());
        result = 31 * result + Objects.hashCode(getFirstName());
        result = 31 * result + Objects.hashCode(getLastName());
        result = 31 * result + Objects.hashCode(getEmail());
        result = 31 * result + Objects.hashCode(getMajor());
        result = 31 * result + Double.hashCode(getCredits());
        result = 31 * result + Double.hashCode(getGpa());
        return result;
    }
}
