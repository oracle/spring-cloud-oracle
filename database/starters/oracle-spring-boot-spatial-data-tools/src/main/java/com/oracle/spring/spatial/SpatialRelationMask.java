// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

/**
 * Supported Oracle Spatial relationship masks for {@code SDO_RELATE}.
 */
public enum SpatialRelationMask {
    ANYINTERACT,
    CONTAINS,
    COVEREDBY,
    COVERS,
    DISJOINT,
    EQUAL,
    INSIDE,
    ON,
    OVERLAPBDYDISJOINT,
    OVERLAPBDYINTERSECT,
    TOUCH;

    String sqlValue() {
        return name();
    }
}
