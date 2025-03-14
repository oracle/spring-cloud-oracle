// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.builder;

import java.util.stream.Stream;

import com.oracle.spring.json.duality.model.movie.Actor;
import com.oracle.spring.json.duality.model.movie.Director;
import com.oracle.spring.json.duality.model.movie.Movie;
import com.oracle.spring.json.duality.model.student.Student;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;

import static com.oracle.spring.json.duality.SpringBootDualityTest.readViewFile;
import static org.assertj.core.api.Assertions.assertThat;

public class DualityViewBuilderTest {
    public static @NotNull Stream<Arguments> entityClasses() {
        return Stream.of(
                Arguments.of(Student.class, "student-update.sql", "update"),
                Arguments.of(Student.class, "student-create.sql", "create"),
                Arguments.of(Actor.class, "actor-create.sql", "create")
        );
    }

    @ParameterizedTest(name = "{1} - {0}")
    @MethodSource("entityClasses")
    public void buildViews(Class<?> entity, String viewFile, String ddlAuto) {
        String expectedView = readViewFile(viewFile);
        DualityViewBuilder dualityViewBuilder = getDualityViewBuilder(ddlAuto);
        String actualView = dualityViewBuilder.build(entity);
        assertThat(expectedView).isEqualTo(actualView);
    }

    private DualityViewBuilder getDualityViewBuilder(String ddlAuto) {
        HibernateProperties hibernateProperties = new HibernateProperties();
        hibernateProperties.setDdlAuto(ddlAuto);
        return new DualityViewBuilder(
                null,
                new JpaProperties(),
                hibernateProperties
        );
    }
}
