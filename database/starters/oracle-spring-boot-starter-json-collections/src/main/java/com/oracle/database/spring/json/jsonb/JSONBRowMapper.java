// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.json.jsonb;

import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.json.stream.JsonParser;
import org.springframework.jdbc.core.RowMapper;

public class JSONBRowMapper<T> implements RowMapper<T> {
    private final JSONB mapper;
    private final Class<T> clazz;
    private final int osonRowNumber;

    public JSONBRowMapper(JSONB mapper, Class<T> clazz, int osonRowNumber) {
        this.mapper = mapper;
        this.clazz = clazz;
        this.osonRowNumber = osonRowNumber;
    }

    public JSONBRowMapper(JSONB mapper, Class<T> clazz) {
        this(mapper, clazz, 1);
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        JsonParser parser = rs.getObject(osonRowNumber, JsonParser.class);
        return mapper.fromOSON(parser, clazz);
    }
}
