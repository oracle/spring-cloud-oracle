/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.TimeZone;

/**
 * ObjectMapper to convert an Object to a JSON String
 */
public final class OCIObjectMapper {
    private static final String DEFAULT_FILTER = "explicitlySetFilter";
    private static final String DEFAULT_TIME_ZONE = "GMT";

    private static final ObjectMapper objectMapper = createObjectMapper(false);

    public static String toPrintableString(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().
                    writeValueAsString(object);
        } catch (Exception ex) {
            return null;
        }
    }

    private static ObjectMapper createObjectMapper(boolean excludeNullValues) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (excludeNullValues) {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm a z");
        df.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIME_ZONE));
        objectMapper.setDateFormat(df);
        final FilterProvider filter = new SimpleFilterProvider()
                .addFilter(DEFAULT_FILTER, SimpleBeanPropertyFilter.serializeAllExcept(Collections.EMPTY_SET));
        objectMapper.setFilterProvider(filter);
        return objectMapper;
    }
}
