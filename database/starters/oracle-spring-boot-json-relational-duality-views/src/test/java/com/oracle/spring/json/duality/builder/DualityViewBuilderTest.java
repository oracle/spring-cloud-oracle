// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.builder;

import java.util.stream.Stream;

import com.oracle.spring.json.duality.model.book.Member;
import com.oracle.spring.json.duality.model.employee.Employee;
import com.oracle.spring.json.duality.model.movie.Actor;
import com.oracle.spring.json.duality.model.products.Order;
import com.oracle.spring.json.duality.model.student.Student;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.oracle.spring.json.duality.SpringBootDualityTest.readViewFile;
import static org.assertj.core.api.Assertions.assertThat;

public class DualityViewBuilderTest {
    public static @NotNull Stream<Arguments> entityClasses() {
        return Stream.of(
                Arguments.of(Student.class, "student-update.sql", "update"),
                Arguments.of(Student.class, "student-create.sql", "create-only"),
                Arguments.of(Actor.class, "actor-create.sql", "create-only"),
                Arguments.of(Order.class, "order-create.sql", "create-only"),
                Arguments.of(Member.class, "member-create-drop.sql", "create-drop"),
                Arguments.of(Employee.class, "employee-create.sql", "create-only")
        );
    }

    @ParameterizedTest(name = "{1} - {0}")
    @MethodSource("entityClasses")
    public void buildViews(Class<?> entity, String viewFile, String ddlAuto) {
        String expectedView = readViewFile(viewFile);
        DualityViewBuilder dualityViewBuilder = getDualityViewBuilder(ddlAuto);
        String actualView = dualityViewBuilder.build(entity);
        System.out.println(actualView);
        assertThat(expectedView).isEqualTo(actualView);
    }

    private DualityViewBuilder getDualityViewBuilder(String ddlAuto) {
        return new DualityViewBuilder(
                null,
                false,
                ddlAuto
        );
    }
}
