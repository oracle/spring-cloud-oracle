// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.json.duality.model.employee;

import jakarta.json.bind.adapter.JsonbAdapter;

public class ManagerAdapter implements JsonbAdapter<Employee, SimpleEmployee> {
    @Override
    public SimpleEmployee adaptToJson(Employee employee) throws Exception {
        SimpleEmployee simpleEmployee = new SimpleEmployee();
        simpleEmployee.set_id(employee.getId());
        simpleEmployee.setName(employee.getName());
        return simpleEmployee;
    }

    @Override
    public Employee adaptFromJson(SimpleEmployee simpleEmployee) throws Exception {
        Employee employee = new Employee();
        employee.setId(simpleEmployee.get_id());
        employee.setName(simpleEmployee.getName());
        return employee;
    }
}
