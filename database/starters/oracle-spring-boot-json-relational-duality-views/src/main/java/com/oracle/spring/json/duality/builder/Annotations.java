// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.builder;

import java.lang.reflect.Field;

import com.oracle.spring.json.duality.annotation.AccessMode;
import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import org.springframework.util.StringUtils;

public final class Annotations {
    public static final String _ID_FIELD = "_id";

    static JoinTable getJoinTableAnnotation( Field f, ManyToMany manyToMany, Class<?> mappedType) {
        JoinTable annotation = f.getAnnotation(JoinTable.class);
        if (annotation != null) {
            return annotation;
        }

        String mappedFieldName = manyToMany.mappedBy();
        if (!StringUtils.hasText(mappedFieldName)) {
            throw new IllegalArgumentException("Mapped field name is required for inverse join on field " + f.getName());
        }

        for (Field field : mappedType.getDeclaredFields()) {
            if (field.getName().equals(mappedFieldName)) {
                JoinTable mappedJoinTable = field.getAnnotation(JoinTable.class);
                if (mappedJoinTable == null) {
                    throw new IllegalArgumentException("Mapped field %s does has no JoinTable annotation".formatted(
                            field.getName()
                    ));
                }
                return mappedJoinTable;
            }
        }
        throw new IllegalArgumentException("No JoinTable found for field " + f.getName());
    }

    static String getNestedViewName(Class<?> javaType,
                                    JsonRelationalDualityView dvAnnotation,
                                    Table tableAnnotation) {
        if (dvAnnotation != null && StringUtils.hasText(dvAnnotation.name())) {
            return dvAnnotation.name();
        }
        return getTableName(javaType, tableAnnotation);
    }

    public static String getViewName(Class<?> javaType, JsonRelationalDualityView dvAnnotation) {
        Table tableAnnotation = javaType.getAnnotation(Table.class);
        final String suffix = "_dv";
        if (dvAnnotation != null && StringUtils.hasText(dvAnnotation.name())) {
            return dvAnnotation.name().toLowerCase();
        }
        if (tableAnnotation != null && StringUtils.hasText(tableAnnotation.name())) {
            return tableAnnotation.name().toLowerCase() + suffix;
        }
        return javaType.getName().toLowerCase() + suffix;
    }

    static String getTableName(Class<?> javaType, Table tableAnnotation) {
        if (tableAnnotation != null && StringUtils.hasText(tableAnnotation.name())) {
            return tableAnnotation.name().toLowerCase();
        }
        return javaType.getName().toLowerCase();
    }

    static boolean isFieldIncluded(Field f) {
        return f.getAnnotation(JsonbTransient.class) == null;
    }


    static String getJsonbPropertyName(Field f) {
        JsonbProperty jsonbProperty = f.getAnnotation(JsonbProperty.class);
        if (jsonbProperty == null || !StringUtils.hasText(jsonbProperty.value())) {
            return f.getName();
        }
        return jsonbProperty.value();
    }

    static String getDatabaseColumnName(Field f) {
        Column column = f.getAnnotation(Column.class);
        if (column != null && StringUtils.hasText(column.name())) {
            return column.name();
        }
        return f.getName();
    }

    static String getAccessModeStr(AccessMode accessMode, ManyToMany manyToMany) {
        StringBuilder sb = new StringBuilder();
        if (manyToMany != null) {
            sb.append("@unnest ");
        }
        if (accessMode != null) {
            if (accessMode.insert()) {
                sb.append("@insert ");
            }
            if (accessMode.update()) {
                sb.append("@update ");
            }
            if (accessMode.delete()) {
                sb.append("@delete ");
            }
        }

        return sb.toString();
    }
}
