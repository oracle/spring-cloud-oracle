/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.core.compartment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompartmentProviderTests {

    @Test
    void testCompartmentProviderWithNullCompartmentId(){
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StaticCompartmentProvider(null);
        });

        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.equals(StaticCompartmentProvider.COMPARTMENT_MISSING_MSG));
    }

    @Test
    void testCompartmentProviderWithCompartmentId(){

        CompartmentProvider compartmentProvider =
                new StaticCompartmentProvider("dummyCompartmentId");

        assertNotNull(compartmentProvider.getCompartmentOCID());
    }

}
