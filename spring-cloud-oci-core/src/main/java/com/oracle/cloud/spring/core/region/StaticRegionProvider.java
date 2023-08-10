/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.core.region;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.RegionProvider;
import org.springframework.util.Assert;

/**
 * Provider to wrap Region details like name for Region configuration
 */
public class StaticRegionProvider implements RegionProvider {

    public static final String INVALID_REGION_MSG = "The region '%s' is not a valid region!";
    private Region region;

    public StaticRegionProvider(String regionName) {
        try {
            region = Region.valueOf(regionName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(INVALID_REGION_MSG, regionName), e);
        }
    }

    public StaticRegionProvider() {
    }

    @Override
    public Region getRegion() {
        return region;
    }

}
