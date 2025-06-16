// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.builder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.stereotype.Component;

import static com.oracle.spring.json.duality.builder.Annotations.getAccessModeStr;
import static com.oracle.spring.json.duality.builder.Annotations.getViewName;

public final class DualityViewBuilder implements DisposableBean {
    private static final String PREFIX = "JSON Relational Duality Views: ";
    private static final int TABLE_OR_VIEW_DOES_NOT_EXIST = 942;

    private final DataSource dataSource;
    private final boolean isShowSql;
    private final RootSnippet rootSnippet;
    private final Map<String, String> dualityViews = new HashMap<>();

    public DualityViewBuilder(DataSource dataSource,
                              boolean isShowSql,
                              String ddlAuto) {
        this.dataSource = dataSource;
        this.isShowSql = isShowSql;
        this.rootSnippet = RootSnippet.fromDdlAuto(ddlAuto);
    }

    void apply() {
        switch (rootSnippet) {
            case NONE -> {
                return;
            }
            case CREATE_DROP -> {
                try {
                    createDrop();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (String ddl : dualityViews.values()) {
            if (isShowSql) {
                System.out.println(PREFIX + ddl);
            }
            if (rootSnippet.equals(RootSnippet.VALIDATE)) {
                // TODO: Handle view validation.
                return;
            }

            runDDL(ddl);
        }
    }

    public String build(Class<?> javaType) {
        JsonRelationalDualityView dvAnnotation = javaType.getAnnotation(JsonRelationalDualityView.class);
        if (dvAnnotation == null) {
            throw new IllegalArgumentException("%s not found for type %s".formatted(
                    JsonRelationalDualityView.class.getSimpleName(), javaType.getName())
            );
        }
        String viewName = getViewName(javaType, dvAnnotation);
        String accessMode = getAccessModeStr(dvAnnotation.accessMode(), null, null);
        ViewEntity ve = new ViewEntity(javaType,
                new StringBuilder(),
                rootSnippet,
                accessMode,
                viewName,
                0,
                false);
        String ddl = ve.build().toString();
        dualityViews.put(viewName, ddl);
        return ddl;
    }

    private void runDDL(String ddl) {
        try (Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(ddl);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void init() throws SQLException {
        createDrop();
    }

    @Override
    public void destroy() throws Exception {
        createDrop();
    }

    private void createDrop() throws SQLException {
        if (rootSnippet.equals(RootSnippet.CREATE_DROP) && !dualityViews.isEmpty()) {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                dropViews(stmt);
            }
        }
    }

    private void dropViews(Statement stmt) {
        final String dropView = "drop view %s";

        for (String view : dualityViews.keySet()) {
            String dropStatement = dropView.formatted(view);
            if (isShowSql) {
                System.out.println(PREFIX + dropStatement);
            }
            try {
                stmt.execute(dropStatement);
            } catch (SQLException e) {
                if (e.getErrorCode() != TABLE_OR_VIEW_DOES_NOT_EXIST) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
