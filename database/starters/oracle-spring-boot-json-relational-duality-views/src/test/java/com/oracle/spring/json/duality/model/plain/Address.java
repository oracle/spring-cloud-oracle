// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.duality.model.plain;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;

import static com.oracle.spring.json.duality.builder.Annotations._ID_FIELD;

@JsonRelationalDualityView
public class Address {
    @JsonbProperty(_ID_FIELD)
    private Long id;

    private String city;
}
