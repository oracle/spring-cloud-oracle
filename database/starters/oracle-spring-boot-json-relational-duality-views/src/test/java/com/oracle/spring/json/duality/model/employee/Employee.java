// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.duality.model.employee;

import java.util.List;
import java.util.Objects;

import com.oracle.spring.json.duality.annotation.AccessMode;
import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import static com.oracle.spring.json.duality.builder.Annotations._ID_FIELD;

@Entity
@Table(name = "employee")
@JsonRelationalDualityView(name = "employee_dv", accessMode = @AccessMode(
        insert = true,
        update = true,
        delete = true
))
@Getter
@Setter
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonbProperty(_ID_FIELD)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "manager_id", referencedColumnName = "id")
    @JsonRelationalDualityView(name = "manager", accessMode = @AccessMode(
            insert = true,
            update = true
    ))
    @JsonbTypeAdapter(ManagerAdapter.class)
    private Employee manager;

    @OneToMany(mappedBy = "manager")
    @JsonRelationalDualityView(name = "reports", accessMode = @AccessMode(
            insert = true,
            update = true
    ))
    @JsonbTypeAdapter(ReportsAdapter.class)
    private List<Employee> reports;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Employee employee)) return false;

        return Objects.equals(getId(), employee.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
