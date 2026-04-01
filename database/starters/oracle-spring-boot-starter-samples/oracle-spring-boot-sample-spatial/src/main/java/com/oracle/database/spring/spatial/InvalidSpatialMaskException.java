// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.spatial;

final class InvalidSpatialMaskException extends RuntimeException {
    InvalidSpatialMaskException(String mask, String supportedValues) {
        super("Unsupported spatial relation mask '%s'. Supported values: %s"
                .formatted(mask, supportedValues));
    }
}
