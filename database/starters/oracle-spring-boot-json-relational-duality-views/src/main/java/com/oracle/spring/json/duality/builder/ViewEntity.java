// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality.builder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
    // Separates JSON keys from database column names
    private static final String SEPARATOR = " : ";
    // Terminal for view entity
    private static final String OBJECT_TERMINAL = "}";
    // Terminal for view array entity
    private static final String ARRAY_TERMINAL = "} ]";
    // Begin array entity
    private static final String BEGIN_ARRAY = "[ {\n";
    // Nesting spacing
    private static final int TAB_WIDTH = 2;

    private final Class<?> javaType;
    private final StringBuilder sb;
    private final RootSnippet rootSnippet;
    private final String accessMode;
    private final String viewName;
    // Tracks number of spaces for key nesting (pretty print)
    private int nesting;
    private final boolean manyToMany;

    // Track views to prevent stacking of nested types
    private final Set<String> views = new HashSet<>();

    private final List<ViewEntity> nestedEntities = new ArrayList<>();

    ViewEntity(Class<?> javaType, StringBuilder sb, String accessMode, String viewName, int nesting, boolean manyToMany) {
        this(javaType, sb, null, accessMode, viewName, nesting, manyToMany);
    }

    ViewEntity(Class<?> javaType, StringBuilder sb, RootSnippet rootSnippet, String accessMode, String viewName, int nesting, boolean manyToMany) {
        this.javaType = javaType;
        this.sb = sb;
        this.rootSnippet = rootSnippet;
        this.accessMode = accessMode;
        this.viewName = viewName;
        this.nesting = nesting;
        this.manyToMany = manyToMany;
        views.add(viewName);
    }

    void addViews(Set<String> views) {
        this.views.addAll(views);
    }

    /**
     * Parse view from javaType.
     * @return this
     */
    ViewEntity build() {
        Table tableAnnotation = javaType.getAnnotation(Table.class);

        if (rootSnippet != null) {
            // Add create view snippet
            sb.append(getStatementPrefix(tableAnnotation));
        } else {
            // Process nested entity
            sb.append(getPadding());
            sb.append(getNestedEntityPrefix(tableAnnotation));
        }

        // Increment the nesting (left padding) after processing an entity.
        incNesting();
        // Parse each field of the javaType.
        for (Field f : javaType.getDeclaredFields()) {
            if (isFieldIncluded(f)) {
                parseField(f);
            }
        }
        for (ViewEntity ve : nestedEntities) {
            ve.addViews(views);
            sb.append(ve.build());
        }
        // Close the entity after processing fields.
        addTrailer(rootSnippet == null);
        if (manyToMany) {
            // Add join table trailer if necessary
            addTrailer(true, ARRAY_TERMINAL);
        }
        return this;
    }

    /**
     * Parse the javaType and tableAnnotation to generate the view prefix, e.g.,
     * 'create force editionable json relational duality view my_view as my table @insert @update @delete {}
     * @param tableAnnotation of the javaType.
     * @return view prefix String.
     */
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
            // Parse the root entity's _id field.
            parseId(f);
        } else if ((dvAnnotation = f.getAnnotation(JsonRelationalDualityView.class)) != null) {
            // Parse the related sub-entity.
            parseRelationalEntity(f, dvAnnotation);
        } else {
            // Parse the field as a database column.
            parseColumn(f);
        }
    }

    /**
     * Parse the view's root _id field.
     * @param f The view's root _id field.
     */
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
        // Add the root _id field to the view.
        addProperty(_ID_FIELD, getDatabaseColumnName(f));
    }

    private void parseRelationalEntity(Field f, JsonRelationalDualityView dvAnnotation) {
        Class<?> entityJavaType = getGenericFieldType(f);
        if (entityJavaType == null) {
            throw new IllegalArgumentException("%s %s annotation must include the entity class".formatted(
                    f.getName(), JsonRelationalDualityView.class.getSimpleName()
            ));
        }

        // Add join table if present.
        ManyToMany manyToMany = f.getAnnotation(ManyToMany.class);
        if (manyToMany != null) {
            parseManyToMany(manyToMany, dvAnnotation, f, entityJavaType);
        }
        // Add nested entity.
        JoinColumn joinColumn = f.getAnnotation(JoinColumn.class);
        parseNestedEntity(entityJavaType, dvAnnotation, manyToMany, joinColumn);
    }

    private boolean visit(String viewName) {
        boolean visited = views.contains(viewName);
        views.add(viewName);
        return visited;
    }

    /**
     * Returns the type of f, or parameterized type of f.
     * @param f to introspect for type information.
     * @return type of f or parameterized type of f.
     */
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

    private void parseNestedEntity(Class<?> entityJavaType,
                                   JsonRelationalDualityView dvAnnotation,
                                   ManyToMany manyToMany,
                                   JoinColumn joinColumn) {
        Table tableAnnotation = entityJavaType.getAnnotation(Table.class);
        String viewEntityName = getNestedViewName(entityJavaType, manyToMany == null ? dvAnnotation : null, tableAnnotation);
        // Prevent infinite recursion
        if (visit(viewEntityName)) {
            return;
        }
        String accessMode = getAccessModeStr(dvAnnotation.accessMode(), manyToMany, joinColumn);
        ViewEntity ve = new ViewEntity(entityJavaType,
                new StringBuilder(),
                accessMode,
                viewEntityName,
                nesting,
                manyToMany != null
        );
        nestedEntities.add(ve);
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
        // Don't parse if we've already visited this entity.
        if (visit(propertyName)) {
            return;
        }
        addProperty(propertyName, joinTable.name(), false);
        sb.append(" ").append(getAccessModeStr(dvAnnotation.accessMode(), null, null));
        sb.append(BEGIN_ARRAY);
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
        addTrailer(addNewLine, OBJECT_TERMINAL);
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
