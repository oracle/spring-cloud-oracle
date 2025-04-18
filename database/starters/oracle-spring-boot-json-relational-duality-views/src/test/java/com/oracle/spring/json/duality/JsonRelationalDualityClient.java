// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality;

import java.util.Optional;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import com.oracle.spring.json.jsonb.JSONB;
import com.oracle.spring.json.jsonb.JSONBRowMapper;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import static com.oracle.spring.json.duality.builder.Annotations.getViewName;

@Component
public class JsonRelationalDualityClient {
    private final JdbcClient jdbcClient;
    private final JSONB jsonb;

    public JsonRelationalDualityClient(JdbcClient jdbcClient, JSONB jsonb) {
        this.jdbcClient = jdbcClient;
        this.jsonb = jsonb;
    }

    public <T> int save(T entity, Class<T> entityJavaType) {
        String viewName = getViewName(entityJavaType, entityJavaType.getAnnotation(JsonRelationalDualityView.class));
        final String sql = """
                insert into %s (data) values (?)
                """.formatted(viewName);

        byte[] oson = jsonb.toOSON(entity);
        return jdbcClient.sql(sql)
                .param(1, oson, OracleTypes.JSON)
                .update();
    }

    public <T, ID> Optional<T> findById(Class<T> entityJavaType, ID id) {
        String viewName = getViewName(entityJavaType, entityJavaType.getAnnotation(JsonRelationalDualityView.class));
        final String sql = """
                select * from %s dv
            where dv.data."_id" = ?
            """.formatted(viewName);

        JSONBRowMapper<T> rowMapper = new JSONBRowMapper<>(jsonb, entityJavaType);
        return jdbcClient.sql(sql)
                .param(1, id)
                .query(rowMapper)
                .optional();
    }
}
