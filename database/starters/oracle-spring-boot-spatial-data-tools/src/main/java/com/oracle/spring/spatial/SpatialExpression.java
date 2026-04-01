// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import java.util.Map;

import org.springframework.util.Assert;

/**
 * Represents a spatial SQL expression that can be used in a select list,
 * insert/update value expression, or order clause while still carrying any
 * required JDBC bind values.
 */
public final class SpatialExpression extends AbstractSpatialJdbcPart {
    private final String expression;

    SpatialExpression(String expression, Map<String, Object> parameters) {
        super(parameters);
        Assert.hasText(expression, "expression must not be blank");
        this.expression = expression;
    }

    /**
     * Returns the raw SQL expression.
     *
     * @return SQL expression
     */
    public String expression() {
        return expression;
    }

    /**
     * Returns this expression as a select-list projection with the given alias.
     *
     * @param alias projection alias
     * @return SQL select-list entry
     */
    public String selection(String alias) {
        Assert.hasText(alias, "alias must not be blank");
        return expression + " " + alias;
    }
}
