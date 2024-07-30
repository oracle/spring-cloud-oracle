/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;

public class StorageContentTypeResolverTests {

    final StorageContentTypeResolver storageContentTypeResolver = new StorageContentTypeResolverImpl();

    @Test
    public void testStorageContentTypeResolverImplWithNullProps() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StorageContentTypeResolverImpl(null);
        });
        assertEquals(StorageContentTypeResolverImpl.ERROR_PROPS_FILE_REQUIRED, exception.getMessage());
    }

    @Test
    public void testResolveContentType() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storageContentTypeResolver.resolveContentType(null);
        });
        assertEquals(StorageContentTypeResolverImpl.ERROR_FILE_NAME_REQUIRED, exception.getMessage());

        storageContentTypeResolver.resolveContentType(".msi");

        new StorageContentTypeResolverImpl(new Properties());
    }
}
