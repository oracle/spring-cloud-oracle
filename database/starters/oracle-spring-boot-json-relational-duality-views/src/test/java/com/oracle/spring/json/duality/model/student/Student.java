// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.model.student;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import com.oracle.spring.json.duality.annotation.AccessMode;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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
@EqualsAndHashCode
@Getter
@Setter
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
}
