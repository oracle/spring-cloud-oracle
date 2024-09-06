// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonduality;

import java.util.UUID;

public class LectureHall {
    private String _id;
    private String name;

    public static LectureHall createLecureHall() {
        LectureHall hall = new LectureHall();
        hall.set_id(UUID.randomUUID().toString());
        return hall;
    }

    public LectureHall() {}

    public LectureHall(String _id, String name) {
        this._id = _id;
        this.name = name;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
