package com.oracle.spring.json.duality.builder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.stereotype.Component;

@Component
public final class DualityViewBuilder implements DisposableBean {
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

    void build(Class<?> javaType, JsonRelationalDualityView dvAnnotation) {
        if (rootSnippet.equals(RootSnippet.NONE)) {
            return;
        }
        ViewEntity ve = new ViewEntity(javaType, new StringBuilder(), rootSnippet, 0);
        String ddl = ve.build().toString();
        if (isShowSql) {
            // TODO: log sql statement
        }
        if (rootSnippet.equals(RootSnippet.VALIDATE)) {
            // TODO: handle duality view validation
            return;
        }
        runDDL(ddl);
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
