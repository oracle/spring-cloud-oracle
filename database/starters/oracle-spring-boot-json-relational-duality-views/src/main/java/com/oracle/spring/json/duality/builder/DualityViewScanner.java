// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.builder;

import java.util.Map;
import java.util.Set;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import com.oracle.spring.json.duality.annotation.JsonRelationalDualityViewScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.util.AnnotatedTypeScanner;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

@Component
final public class DualityViewScanner {
    private final DualityViewBuilder dualityViewBuilder;
    private final ApplicationContext applicationContext;
    private final AnnotatedTypeScanner scanner;

    public DualityViewScanner(DualityViewBuilder dualityViewBuilder,
                              ApplicationContext applicationContext,
                              @Qualifier("jsonRelationalDualityViewScanner") AnnotatedTypeScanner scanner) {
        this.dualityViewBuilder = dualityViewBuilder;
        this.applicationContext = applicationContext;
        this.scanner = scanner;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void scan() {
        Map<String, Object> springBootApplications = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        for (Map.Entry<String, Object> entry : springBootApplications.entrySet()) {
            Class<?> mainClass = ClassUtils.getUserClass(entry.getValue().getClass());
            JsonRelationalDualityViewScan dvScan = mainClass.getAnnotation(JsonRelationalDualityViewScan.class);
            if (dvScan == null) {
                scanPackage(mainClass.getPackageName());
            } else {
                applyDvScan(dvScan);
            }
        }
    }

    private void applyDvScan(JsonRelationalDualityViewScan dvScan) {
        if (dvScan.basePackages() != null) {
            for (String pkg : dvScan.basePackages()) {
                scanPackage(pkg);
            }
        }
        for (Class<?> javaType : dvScan.basePackageClasses()) {
            applyClass(javaType);
        }
    }

    private void scanPackage(String packageName) {
        Set<Class<?>> types = scanner.findTypes(packageName);
        for (Class<?> type : types) {
            applyClass(type);
        }
    }

    private void applyClass(Class<?> javaType) {
        JsonRelationalDualityView dvAnnotation = javaType.getAnnotation(JsonRelationalDualityView.class);
        if (dvAnnotation != null) {
            dualityViewBuilder.apply(javaType);
        }
    }
}
