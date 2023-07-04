/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import org.springframework.lang.Nullable;

import java.io.IOException;

/**
 * Factory interface to instantiate custom/default StorageOutputStream.
 */
public interface StorageOutputStreamProvider {

    StorageOutputStream create(String bucket, String key, @Nullable StorageObjectMetadata metadata) throws IOException;

    /**
     * Creates StorageOutputStream instance based on bean configuration.
     * @param location StorageLocation instance pointing to a specific object.
     * @param metadata Metadata information to create object in the storage service.
     * @return OutputStream instance
     * @throws IOException
     */
    default StorageOutputStream create(StorageLocation location, @Nullable StorageObjectMetadata metadata) throws IOException {
        return create(location.getBucket(), location.getObject(), metadata);
    }
}
