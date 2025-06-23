// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.model.products;

import com.oracle.spring.json.duality.annotation.AccessMode;
import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.util.Date;
import java.util.Objects;

import static com.oracle.spring.json.duality.builder.Annotations._ID_FIELD;

@JsonRelationalDualityView(accessMode = @AccessMode(insert = true))
@Table(name = "orders")
public class Order {
    @JsonbProperty(_ID_FIELD)
    @Column(name = "order_id")
    private Long id;
    @JsonRelationalDualityView(name = "product")
    @Column(name = "products")
    private Product product;
    private Integer quantity;
    @Column(name = "order_date")
    private Date orderDate;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Order order)) return false;

        return Objects.equals(getId(), order.getId()) && Objects.equals(getQuantity(), order.getQuantity()) && Objects.equals(getOrderDate(), order.getOrderDate());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getId());
        result = 31 * result + Objects.hashCode(getQuantity());
        result = 31 * result + Objects.hashCode(getOrderDate());
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
}