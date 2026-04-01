// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

/**
 * {@link RowMapper} that reads a projected GeoJSON column from a JDBC result
 * set.
 */
public final class OracleSpatialGeoJsonRowMapper implements RowMapper<String> {
    private final String columnLabel;

    /**
     * Creates a row mapper for the given projected GeoJSON column label.
     *
     * @param columnLabel result-set column label
     */
    public OracleSpatialGeoJsonRowMapper(String columnLabel) {
        Assert.hasText(columnLabel, "columnLabel must not be blank");
        this.columnLabel = columnLabel;
    }

    @Override
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString(columnLabel);
    }
}
