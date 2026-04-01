// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import java.util.Map;

import org.springframework.util.Assert;

/**
 * Represents a spatial predicate suitable for use in a SQL {@code WHERE}
 * clause while carrying the bind values needed by Spring JDBC.
 */
public final class SpatialPredicate extends AbstractSpatialJdbcPart {
    private final String clause;

    SpatialPredicate(String clause, Map<String, Object> parameters) {
        super(parameters);
        Assert.hasText(clause, "clause must not be blank");
        this.clause = clause;
    }

    /**
     * Returns the SQL predicate clause.
     *
     * @return SQL predicate
     */
    public String clause() {
        return clause;
    }
}
