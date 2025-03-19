// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.model.products;

import com.oracle.spring.json.duality.annotation.AccessMode;
import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import static com.oracle.spring.json.duality.builder.Annotations._ID_FIELD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@JsonRelationalDualityView(
    name = "product_dv",
    accessMode = @AccessMode(insert = true)
)
public class Product {
    @JsonbProperty(_ID_FIELD)
    @Column(name = "product_id")
    private Long id;
    private String name;
    private Double price;
}
