// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.builder;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.util.AnnotatedTypeScanner;
import org.springframework.util.StringUtils;

@AutoConfiguration
@EnableConfigurationProperties(DualityViewConfigurationProperties.class)
public class DualityViewAutoConfiguration {
    @Bean
    @Qualifier("jsonRelationalDualityViewScanner")
    public AnnotatedTypeScanner scanner(ResourceLoader resourceLoader, Environment environment) {
        AnnotatedTypeScanner dvScanner = new AnnotatedTypeScanner(JsonRelationalDualityView.class);

        dvScanner.setResourceLoader(resourceLoader);
        dvScanner.setEnvironment(environment);
        return dvScanner;
    }

    @Bean
    DualityViewBuilder dualityViewBuilder(DataSource dataSource,
                                          JpaProperties jpaProperties,
                                          HibernateProperties hibernateProperties,
                                          DualityViewConfigurationProperties dvProperties) {
        boolean isShowSQL = dvProperties != null && dvProperties.isShowSql() != null ?
                dvProperties.isShowSql() :
                jpaProperties.isShowSql();
        String ddlAuto = dvProperties != null && StringUtils.hasText(dvProperties.getDdlAuto()) ?
                dvProperties.getDdlAuto() :
                hibernateProperties.getDdlAuto();

        return new DualityViewBuilder(
                dataSource,
                isShowSQL,
                ddlAuto
        );
    }
}
