// Copyright (c) 2024, 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.test;

import java.util.Objects;

public class StudentDetails {
    String major;
    double gpa;
    double credits;

    public StudentDetails() {}

    public StudentDetails(String major, double gpa, double credits) {
        this.major = major;
        this.gpa = gpa;
        this.credits = credits;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof StudentDetails that)) return false;

        return Double.compare(getGpa(), that.getGpa()) == 0 && Double.compare(getCredits(), that.getCredits()) == 0 && Objects.equals(getMajor(), that.getMajor());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getMajor());
        result = 31 * result + Double.hashCode(getGpa());
        result = 31 * result + Double.hashCode(getCredits());
        return result;
    }
}
