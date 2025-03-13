// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality;

import com.oracle.spring.json.jsonb.JSONB;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JsonRelationalDualityClient {
    private final JdbcClient jdbcClient;
    private final JSONB jsonb;

    public JsonRelationalDualityClient(JdbcClient jdbcClient, JSONB jsonb) {
        this.jdbcClient = jdbcClient;
        this.jsonb = jsonb;
    }

    public <T> T save(T entity) {
        byte[] oson = jsonb.toOSON(entity);

        final String sql = """
                
                """;
        return null;
    }

    public <T, ID> T findById(Class<T> entityJavaType, ID id) {
        return null;
    }
}
