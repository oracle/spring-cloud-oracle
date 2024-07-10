// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package com.oracle.cloud.spring.adb;

public record AutonomousDbDetails (
    String compartmentId,
    String displayName,
    String id,
    String dbName,
    String lifecycleState,
    String timeCreated,
    Float computeCount,
    Integer dataStorageSizeInGBs,
    String licenseModel,
    String serviceConsoleUrl
) { }
