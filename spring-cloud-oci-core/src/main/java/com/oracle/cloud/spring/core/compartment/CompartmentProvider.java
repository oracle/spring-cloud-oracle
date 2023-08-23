/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.core.compartment;

/**
 * Interface to provide Compartment details, like OCID, for Compartment configuration.
 */
public interface CompartmentProvider {

    String getCompartmentOCID();
}
