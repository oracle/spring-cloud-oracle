// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.model.products;

import java.util.Objects;

import com.oracle.spring.json.duality.annotation.AccessMode;
import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import static com.oracle.spring.json.duality.builder.Annotations._ID_FIELD;

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

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Product product)) return false;

        return Objects.equals(getId(), product.getId()) && Objects.equals(getName(), product.getName()) && Objects.equals(getPrice(), product.getPrice());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getId());
        result = 31 * result + Objects.hashCode(getName());
        result = 31 * result + Objects.hashCode(getPrice());
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
