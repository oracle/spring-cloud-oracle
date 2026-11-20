// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.duality.model.plain;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;

import static com.oracle.spring.json.duality.builder.Annotations._ID_FIELD;

// Plain class without @Table: the table name is the lower case simple class name.
@JsonRelationalDualityView
public class Customer {
    @JsonbProperty(_ID_FIELD)
    private Long id;

    private String name;

    @JsonRelationalDualityView
    private Address address;
}
