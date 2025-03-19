// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.builder;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.util.AnnotatedTypeScanner;

@Configuration
public class ScannerConfiguration {
    @Bean
    @Qualifier("jsonRelationalDualityViewScanner")
    public AnnotatedTypeScanner scanner(ResourceLoader resourceLoader, Environment environment) {
        AnnotatedTypeScanner dvScanner = new AnnotatedTypeScanner(JsonRelationalDualityView.class);

        dvScanner.setResourceLoader(resourceLoader);
        dvScanner.setEnvironment(environment);
        return dvScanner;
    }
}
