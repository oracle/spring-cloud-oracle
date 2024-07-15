/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import java.util.regex.Pattern;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Storage location parser for Object Storage URI.
 */
public class StorageLocation {
    private static final Pattern URL_PATTERN = Pattern.compile("(https://)?(.+?\\.)?objectstorage\\.(.+?)\\.(oraclecloud\\.com|oci\\.customer-oci\\.com)(.+?)");

    private static final String BUCKET_DELIMITER = "/b/";

    private static final String OBJECT_DELIMITER = "/o/";

    static final String ERROR_BUCKET_REQUIRED = "bucket is required";

    static final String ERROR_OBJECT_REQUIRED = "object is required";

    static final String ERROR_INVALID_BUCKET = "does not contain a valid bucket name";

    private final String bucket;

    private final String object;

    StorageLocation(String bucket, String object) {
        Assert.notNull(bucket, ERROR_BUCKET_REQUIRED);
        Assert.notNull(object, ERROR_OBJECT_REQUIRED);

        this.bucket = bucket;
        this.object = object;
    }

    String getBucket() {
        return bucket;
    }

    String getObject() {
        return object;
    }

    @Override
    public String toString() {
        return String.format("Location{bucket='%s', object='%s'}", bucket, object);
    }

    /**
     * Get the instance of {@link StorageLocation} by parsing storage location URI.
     * @param location URI string of object stored on OCI Storage service.
     * @return StorageLocation
     */
    @Nullable
    public static StorageLocation resolve(String location) {
        if (isSimpleStorageResource(location)) {
            return new StorageLocation(resolveBucketName(location), resolveObjectName(location));
        }

        return null;
    }

    /**
     * Checks whether the location URI is an OCI Object Storage URI.
     * @param location Resource location.
     * @return Boolean
     */
    static boolean isSimpleStorageResource(String location) {
        Assert.notNull(location, "Location must not be null");
        return URL_PATTERN.matcher(location).matches();
    }

    public static String resolveBucketName(String location) {
        int bucketStartIndex = location.indexOf(BUCKET_DELIMITER);
        int bucketEndIndex = location.indexOf(OBJECT_DELIMITER);
        if (bucketStartIndex == -1 || location.length() < BUCKET_DELIMITER.length()) {
            throw new IllegalArgumentException("The location :'" + location + "' " + ERROR_INVALID_BUCKET);
        }
        return location.substring(bucketStartIndex + BUCKET_DELIMITER.length(), bucketEndIndex == -1 ? location.length() - 1 : bucketEndIndex);
    }

    public static String resolveObjectName(String location) {
        int objectStartIndex = location.indexOf(OBJECT_DELIMITER);

        if (objectStartIndex == -1 || location.length() < OBJECT_DELIMITER.length()) {
            throw new IllegalArgumentException(ERROR_OBJECT_REQUIRED);
        }
        return location.substring(objectStartIndex + OBJECT_DELIMITER.length());
    }
}
