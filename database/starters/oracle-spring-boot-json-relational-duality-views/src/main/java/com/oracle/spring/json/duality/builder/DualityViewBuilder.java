// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.builder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import javax.sql.DataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.stereotype.Component;

import static com.oracle.spring.json.duality.builder.Annotations.getAccessModeStr;
import static com.oracle.spring.json.duality.builder.Annotations.getViewName;

@Component
public final class DualityViewBuilder implements DisposableBean {
    private static final String PREFIX = "JSON Relational Duality Views: ";

    private final DataSource dataSource;
    private final boolean isShowSql;
    private final RootSnippet rootSnippet;
    private final List<String> dualityViews = new ArrayList<>();

    public DualityViewBuilder(DataSource dataSource,
                              JpaProperties jpaProperties,
                              HibernateProperties hibernateProperties) {
        this.dataSource = dataSource;
        this.isShowSql = jpaProperties.isShowSql();
        this.rootSnippet = RootSnippet.fromDdlAuto(
                hibernateProperties.getDdlAuto()
        );
    }

    void apply(Class<?> javaType) {
        if (rootSnippet.equals(RootSnippet.NONE)) {
            return;
        }
        String ddl = build(javaType);
        if (isShowSql) {
            System.out.println(PREFIX + ddl);
        }
        if (rootSnippet.equals(RootSnippet.VALIDATE)) {
            // TODO: Handle view validation.
            return;
        }

        runDDL(ddl);
    }

    String build(Class<?> javaType) {
        JsonRelationalDualityView dvAnnotation = javaType.getAnnotation(JsonRelationalDualityView.class);
        if (dvAnnotation == null) {
            throw new IllegalArgumentException("%s not found for type %s".formatted(
                    JsonRelationalDualityView.class.getSimpleName(), javaType.getName())
            );
        }
        String viewName = getViewName(javaType, dvAnnotation);
        String accessMode = getAccessModeStr(dvAnnotation.accessMode());
        ViewEntity ve = new ViewEntity(javaType,
                new StringBuilder(),
                rootSnippet,
                accessMode,
                viewName,
                0);
        return ve.build().toString();
    }

    private void runDDL(String ddl) {
        try (Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(ddl);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void destroy() throws Exception {
        if (rootSnippet.equals(RootSnippet.CREATE_DROP) && !dualityViews.isEmpty()) {
            final String dropView = """
                    drop view %s
                    """;
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                for (String view : dualityViews) {
                    stmt.execute(dropView.formatted(view));
                }
            }
        }
    }
}
