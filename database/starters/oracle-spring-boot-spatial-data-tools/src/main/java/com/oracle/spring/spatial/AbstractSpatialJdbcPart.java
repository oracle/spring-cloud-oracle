// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import java.util.Map;

import org.springframework.jdbc.core.simple.JdbcClient;

abstract class AbstractSpatialJdbcPart implements SpatialJdbcBindable {
    private final Map<String, Object> parameters;

    AbstractSpatialJdbcPart(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public JdbcClient.StatementSpec bind(JdbcClient.StatementSpec statement) {
        JdbcClient.StatementSpec current = statement;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            current = current.param(entry.getKey(), entry.getValue());
        }
        return current;
    }
}
