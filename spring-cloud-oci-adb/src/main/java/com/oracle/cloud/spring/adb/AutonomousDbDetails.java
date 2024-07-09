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
