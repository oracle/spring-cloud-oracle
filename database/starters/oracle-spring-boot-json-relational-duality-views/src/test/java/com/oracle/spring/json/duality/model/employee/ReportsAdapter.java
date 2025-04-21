// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.duality.model.employee;

import java.util.List;

import jakarta.json.bind.adapter.JsonbAdapter;

public class ReportsAdapter implements JsonbAdapter<List<Employee>, List<SimpleEmployee>> {

    @Override
    public List<SimpleEmployee> adaptToJson(List<Employee> employees) throws Exception {
        return employees.stream().map(e -> {
            SimpleEmployee simpleEmployee = new SimpleEmployee();
            simpleEmployee.set_id(e.getId());
            simpleEmployee.setName(e.getName());
            return simpleEmployee;
        }).toList();
    }

    @Override
    public List<Employee> adaptFromJson(List<SimpleEmployee> simpleEmployees) throws Exception {
        return simpleEmployees.stream().map(s -> {
            Employee employee = new Employee();
            employee.setId(s.get_id());
            employee.setName(s.getName());
            return employee;
        }).toList();
    }
}
