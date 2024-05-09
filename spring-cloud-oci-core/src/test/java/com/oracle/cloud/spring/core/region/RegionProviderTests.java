/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.core.region;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegionProviderTests {

    @Test
    void testInvalidRegion(){
        final String region = "foo";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StaticRegionProvider(region);
        });

        String actualMessage = exception.getMessage();

        assertEquals(actualMessage, StaticRegionProvider.INVALID_REGION_MSG.formatted(region));
    }

    @Test
    void testValidRegion(){
        final String region = "us-ashburn-1";
        StaticRegionProvider regionProvider = new StaticRegionProvider(region);

        assertNotNull(regionProvider.getRegion());
    }

    @Test
    void testEmptyRegion(){
        StaticRegionProvider regionProvider = new StaticRegionProvider();

        assertNull(regionProvider.getRegion());
    }
}
