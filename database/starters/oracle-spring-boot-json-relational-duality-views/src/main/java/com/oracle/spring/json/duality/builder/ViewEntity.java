package com.oracle.spring.json.duality.builder;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityViewEntity;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import static com.oracle.spring.json.duality.builder.Annotations._ID_FIELD;
import static com.oracle.spring.json.duality.builder.Annotations.getAccessModeStr;
import static com.oracle.spring.json.duality.builder.Annotations.getDatabaseColumnName;
import static com.oracle.spring.json.duality.builder.Annotations.getJoinTableAnnotation;
import static com.oracle.spring.json.duality.builder.Annotations.getJsonbPropertyName;
import static com.oracle.spring.json.duality.builder.Annotations.getTableName;
import static com.oracle.spring.json.duality.builder.Annotations.getViewEntityName;
import static com.oracle.spring.json.duality.builder.Annotations.isRelationalEntity;

final class ViewEntity {

    private static final String SEPARATOR = " : ";
    private static final String END_ENTITY = "}";
    private static final String BEGIN_ENTITY = " {\n";
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
            parseField(f);
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
        return "%s : %s %s{\n".formatted(
                viewName, tableName, accessMode
        );
    }

    private void parseField(Field f) {
        Id id = f.getAnnotation(Id.class);
        if (id != null && rootSnippet != null) {
            parseId(f);
        } else if (isRelationalEntity(f)) {
            JsonRelationalDualityViewEntity viewEntityAnnotation = f.getAnnotation(JsonRelationalDualityViewEntity.class);
            // The entity should not be included in the view.
            if (viewEntityAnnotation == null) {
                return;
            }
            parseRelationalEntity(f, viewEntityAnnotation);
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

    private void parseRelationalEntity(Field f, JsonRelationalDualityViewEntity viewEntityAnnotation) {
        Class<?> entityJavaType = viewEntityAnnotation.entity();
        if (entityJavaType == null) {
            throw new IllegalArgumentException("%s %s annotation must include the entity class".formatted(
                    f.getName(), JsonRelationalDualityViewEntity.class.getSimpleName()
            ));
        }

        // Prevent stack overflow of circular references.
        if (parentTypes.contains(entityJavaType)) {
            return;
        }
        // Add join table if present.
        ManyToMany manyToMany = f.getAnnotation(ManyToMany.class);
        if (manyToMany != null) {
            parseManyToMany(manyToMany, f, entityJavaType);
        }
        // Add nested entity.
        parseNestedEntity(entityJavaType, viewEntityAnnotation);
        // Additional trailer for join table if present.
        if (manyToMany != null) {
            addTrailer(true);
        }
    }

    private void parseNestedEntity(Class<?> entityJavaType, JsonRelationalDualityViewEntity viewEntityAnnotation) {
        Table tableAnnotation = entityJavaType.getAnnotation(Table.class);
        String viewEntityName = getViewEntityName(entityJavaType, viewEntityAnnotation, tableAnnotation);
        String accessMode = getAccessModeStr(viewEntityAnnotation.accessMode());
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

    private void parseManyToMany(ManyToMany manyToMany, Field f, Class<?> entityJavaType) {
        JoinTable joinTable = getJoinTableAnnotation(f, manyToMany, entityJavaType);
        sb.append(getPadding());
        sb.append(joinTable.name());
        sb.append(BEGIN_ENTITY);
        incNesting();
        addJoinColumns(joinTable.joinColumns());
        addJoinColumns(joinTable.inverseJoinColumns());
    }

    private void addJoinColumns(JoinColumn[] joinColumns) {
        for (JoinColumn joinColumn : joinColumns) {
            addProperty(joinColumn.name(), joinColumn.name());
        }
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

    private void addTrailer(boolean addNewline) {
        decNesting();
        if (nesting > 0) {
            sb.append(getPadding());
        }
        sb.append(END_ENTITY);
        if (addNewline) {
            sb.append("\n");
        }
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
