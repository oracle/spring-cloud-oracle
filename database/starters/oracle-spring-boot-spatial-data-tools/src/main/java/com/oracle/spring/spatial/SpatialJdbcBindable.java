// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Contract for spatial query parts that can apply their bind parameters to a
 * {@link JdbcClient.StatementSpec}.
 */
public interface SpatialJdbcBindable {
    /**
     * Applies this part's bind parameters to the given statement.
     *
     * @param statement statement to update
     * @return updated statement
     */
    JdbcClient.StatementSpec bind(JdbcClient.StatementSpec statement);
}
