/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.core.compartment;

import org.springframework.util.Assert;

/**
 * Provider to wrap Compartment details like OCID for Compartment configuration
 */
public class StaticCompartmentProvider implements CompartmentProvider {

    public static final String COMPARTMENT_MISSING_MSG = "compartmentOCID is required";
    private final String compartmentOCID;

    public StaticCompartmentProvider(String compartmentOCID) {
        Assert.notNull(compartmentOCID, COMPARTMENT_MISSING_MSG);
        this.compartmentOCID = compartmentOCID;
    }

    @Override
    public String getCompartmentOCID() {
        return compartmentOCID;
    }

}
