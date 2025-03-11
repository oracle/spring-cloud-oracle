package com.oracle.spring.json.duality.builder;

import java.lang.reflect.Field;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.util.StringUtils;

final class ViewEntity {
    private static final String _ID_FIELD = "_id";
    private static final String SEPARATOR = " : ";
    private static final String TRAILER = "}";
    private static final int TAB_WIDTH = 2;

    private final Class<?> javaType;
    private final StringBuilder sb;
    private final RootSnippet rootSnippet;
    private int nesting;

    ViewEntity(Class<?> javaType, StringBuilder sb, RootSnippet rootSnippet, int nesting) {
        this.javaType = javaType;
        this.sb = sb;
        this.rootSnippet = rootSnippet;
        this.nesting = nesting;
    }

    ViewEntity build() {
        if (rootSnippet != null) {
            JsonRelationalDualityView dvAnnotation = javaType.getAnnotation(JsonRelationalDualityView.class);
            Table tableAnnotation = javaType.getAnnotation(Table.class);
            sb.append(getStatementPrefix(javaType, dvAnnotation, tableAnnotation));
        }

        incNesting();
        for (Field f : javaType.getDeclaredFields()) {
            parseField(f);
        }
        decNesting();
        addTrailer();
        return this;
    }

    private String getStatementPrefix(Class<?> javaType,
                                      JsonRelationalDualityView dvAnnotation,
                                      Table tableAnnotation) {
        String viewName = getViewName(javaType, dvAnnotation, tableAnnotation);
        String tableName = getTableName(javaType, tableAnnotation);
        return "%s %s as %s @insert @update @delete {\n".formatted(
                rootSnippet.getSnippet(), viewName, tableName
        );
    }

    private String getViewName(Class<?> javaType,
                              JsonRelationalDualityView dvAnnotation,
                              Table tableAnnotation) {
        final String suffix = "_dv";
        if (dvAnnotation != null && StringUtils.hasText(dvAnnotation.name())) {
            return dvAnnotation.name();
        }
        if (tableAnnotation != null && StringUtils.hasText(tableAnnotation.name())) {
            return tableAnnotation.name() + suffix;
        }
        return javaType.getName() + suffix;
    }

    private String getTableName(Class<?> javaType, Table tableAnnotation) {
        if (tableAnnotation != null && StringUtils.hasText(tableAnnotation.name())) {
            return tableAnnotation.name();
        }
        return javaType.getName();
    }

    private void parseField(Field f) {
        Id id = f.getAnnotation(Id.class);
        if (id != null && rootSnippet != null) {
            parseId(f);
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

    private void parseColumn(Field f) {
        addProperty(getJsonbPropertyName(f), getDatabaseColumnName(f));
    }

    private String getJsonbPropertyName(Field f) {
        JsonbProperty jsonbProperty = f.getAnnotation(JsonbProperty.class);
        if (jsonbProperty == null || !StringUtils.hasText(jsonbProperty.value())) {
            return f.getName();
        }
        return jsonbProperty.value();
    }

    private String getDatabaseColumnName(Field f) {
        Column column = f.getAnnotation(Column.class);
        if (column != null && StringUtils.hasText(column.name())) {
            return column.name();
        }
        return f.getName();
    }

    private void addProperty(String jsonbPropertyName, String databaseColumnName) {
        sb.append(getPadding());
        if (jsonbPropertyName.equals(databaseColumnName)) {
            sb.append(jsonbPropertyName);
        } else {
            sb.append(jsonbPropertyName)
                    .append(SEPARATOR)
                    .append(databaseColumnName);
        }
        sb.append("\n");
    }

    private void addTrailer() {
        if (nesting > 0) {
            sb.append(getPadding());
        }
        sb.append(TRAILER);
    }

    private String getPadding() {
        return String.format("%" + nesting + "s", " ");
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
