// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.builder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import org.springframework.util.StringUtils;

import static com.oracle.spring.json.duality.builder.Annotations._ID_FIELD;
import static com.oracle.spring.json.duality.builder.Annotations.getAccessModeStr;
import static com.oracle.spring.json.duality.builder.Annotations.getDatabaseColumnName;
import static com.oracle.spring.json.duality.builder.Annotations.getJoinTableAnnotation;
import static com.oracle.spring.json.duality.builder.Annotations.getJsonbPropertyName;
import static com.oracle.spring.json.duality.builder.Annotations.getTableName;
import static com.oracle.spring.json.duality.builder.Annotations.getNestedViewName;
import static com.oracle.spring.json.duality.builder.Annotations.isFieldIncluded;

final class ViewEntity {
    private static final String SEPARATOR = " : ";
    private static final String END_ENTITY = "}";
    private static final String END_ARRAY_ENTITY = "} ]";
    private static final String BEGIN_ARRAY_ENTITY = "[ {\n";
    private static final int TAB_WIDTH = 2;

    private final Class<?> javaType;
    private final StringBuilder sb;
    private final RootSnippet rootSnippet;
    private final String accessMode;
    private final String viewName;
    private int nesting;

    // Track parent types to prevent stacking of nested types
    private final Set<Class<?>> parentTypes = new HashSet<>();

    ViewEntity(Class<?> javaType, StringBuilder sb, String accessMode, String viewName, int nesting) {
        this(javaType, sb, null, accessMode, viewName, nesting);
    }

    ViewEntity(Class<?> javaType, StringBuilder sb, RootSnippet rootSnippet, String accessMode, String viewName, int nesting) {
        this.javaType = javaType;
        this.sb = sb;
        this.rootSnippet = rootSnippet;
        this.accessMode = accessMode;
        this.viewName = viewName;
        this.nesting = nesting;
        parentTypes.add(javaType);
    }

    void addParentTypes(Set<Class<?>> parentTypes) {
        this.parentTypes.addAll(parentTypes);
    }

    ViewEntity build() {
        Table tableAnnotation = javaType.getAnnotation(Table.class);

        if (rootSnippet != null) {
            // Root duality view statement
            sb.append(getStatementPrefix(tableAnnotation));
        } else {
            sb.append(getPadding());
            sb.append(getNestedEntityPrefix(tableAnnotation));
        }

        incNesting();
        for (Field f : javaType.getDeclaredFields()) {
            if (isFieldIncluded(f)) {
                parseField(f);
            }
        }
        addTrailer(rootSnippet == null);
        return this;
    }

    private String getStatementPrefix(Table tableAnnotation) {
        String tableName = getTableName(javaType, tableAnnotation);
        return "%s %s as %s %s{\n".formatted(
                rootSnippet.getSnippet(), viewName, tableName, accessMode
        );
    }

    private String getNestedEntityPrefix(Table tableAnnotation) {
        String tableName = getTableName(javaType, tableAnnotation);
        if (tableName.equals(viewName)) {
            return "%s %s{\n".formatted(tableName, accessMode);
        }
        return "%s%s%s %s{\n".formatted(
                viewName, SEPARATOR, tableName, accessMode
        );
    }

    private void parseField(Field f) {
        JsonRelationalDualityView dvAnnotation;
        Id id = f.getAnnotation(Id.class);
        if (id != null && rootSnippet != null) {
            parseId(f);
        } else if ((dvAnnotation = f.getAnnotation(JsonRelationalDualityView.class)) != null) {
            parseRelationalEntity(f, dvAnnotation);
        } else {
            parseColumn(f);
        }
    }

    private void parseId(Field f) {
        String jsonbPropertyName = getJsonbPropertyName(f);
        if (!jsonbPropertyName.equals(_ID_FIELD)) {
            throw new IllegalArgumentException("@Id Field %s must be named \"%s\" or annotated with @%s(\"%s\")".formatted(
                    f.getName(),
                    _ID_FIELD,
                    JsonbProperty.class.getSimpleName(),
                    _ID_FIELD
            ));
        }
        addProperty(_ID_FIELD, getDatabaseColumnName(f));
    }

    private void parseRelationalEntity(Field f, JsonRelationalDualityView dvAnnotation) {
        Class<?> entityJavaType = getGenericFieldType(f);
        if (entityJavaType == null) {
            throw new IllegalArgumentException("%s %s annotation must include the entity class".formatted(
                    f.getName(), JsonRelationalDualityView.class.getSimpleName()
            ));
        }

        // Prevent stack overflow of circular references.
        if (parentTypes.contains(entityJavaType)) {
            return;
        }
        // Add join table if present.
        ManyToMany manyToMany = f.getAnnotation(ManyToMany.class);
        if (manyToMany != null) {
            parseManyToMany(manyToMany, dvAnnotation, f, entityJavaType);
        }
        // Add nested entity.
        parseNestedEntity(entityJavaType, dvAnnotation, manyToMany);
        // Additional trailer for join table if present.
        if (manyToMany != null) {
            addTrailer(true, END_ARRAY_ENTITY);
        }
    }

    private Class<?> getGenericFieldType(Field f) {
        Type genericType = f.getGenericType();
        if (genericType instanceof ParameterizedType p) {
            Type type = p.getActualTypeArguments()[0];
            if (type instanceof Class<?> c) {
                return c;
            }
            throw new IllegalStateException("failed to process type: " + type);
        }
        return f.getType();
    }

    private void parseNestedEntity(Class<?> entityJavaType, JsonRelationalDualityView dvAnnotation, ManyToMany manyToMany) {
        Table tableAnnotation = entityJavaType.getAnnotation(Table.class);
        String viewEntityName = getNestedViewName(entityJavaType, manyToMany == null ? dvAnnotation : null, tableAnnotation);
        String accessMode = getAccessModeStr(dvAnnotation.accessMode(), manyToMany);
        ViewEntity ve = new ViewEntity(entityJavaType,
                new StringBuilder(),
                accessMode,
                viewEntityName,
                nesting
        );
        ve.addParentTypes(parentTypes);
        sb.append(ve.build());
    }

    private void parseColumn(Field f) {
        addProperty(getJsonbPropertyName(f), getDatabaseColumnName(f));
    }

    private void parseManyToMany(ManyToMany manyToMany, JsonRelationalDualityView dvAnnotation,  Field f, Class<?> entityJavaType) {
        JoinTable joinTable = getJoinTableAnnotation(f, manyToMany, entityJavaType);
        String propertyName = dvAnnotation.name();
        if (!StringUtils.hasText(propertyName)) {
            propertyName = getJsonbPropertyName(f);
        }
        addProperty(propertyName, joinTable.name(), false);
        sb.append(" ").append(getAccessModeStr(dvAnnotation.accessMode(), null));
        sb.append(BEGIN_ARRAY_ENTITY);
        incNesting();
    }

    private void addProperty(String jsonbPropertyName, String databaseColumnName, boolean addNewLine) {
        sb.append(getPadding());
        if (jsonbPropertyName.equals(databaseColumnName)) {
            sb.append(jsonbPropertyName);
        } else {
            sb.append(jsonbPropertyName)
                    .append(SEPARATOR)
                    .append(databaseColumnName);
        }
        if (addNewLine) {
            sb.append("\n");
        }
    }

    private void addProperty(String jsonbPropertyName, String databaseColumnName) {
        addProperty(jsonbPropertyName, databaseColumnName, true);
    }

    private void addTrailer(boolean addNewLine) {
        addTrailer(addNewLine, END_ENTITY);
    }

    private void addTrailer(boolean addNewLine, String terminal) {
        decNesting();
        if (nesting > 0) {
            sb.append(getPadding());
        }
        sb.append(terminal);
        if (addNewLine) {
            sb.append("\n");
        }
    }

    private String getPadding() {
        return " ".repeat(nesting);
    }

    private void incNesting() {
        nesting += TAB_WIDTH;
    }

    private void decNesting() {
        nesting -= TAB_WIDTH;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
