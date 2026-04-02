/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.WritableResource;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleStorageResourceTests {

    @Test
    void testCreate() {
        StorageTestSupport.FakeObjectStorageClient osClient = StorageTestSupport.newObjectStorageClient();
        assertNotNull(OracleStorageResource.create(
                "https://objectstorage.us-chicago-1.oraclecloud.com/n/namespace/b/mybucket/o/myobject", osClient));
        assertNull(OracleStorageResource.create("classpath:test.txt", osClient));
    }

    @Test
    void testGetInputStream() throws Exception {
        StorageTestSupport.FakeObjectStorageClient osClient = StorageTestSupport.newObjectStorageClient();
        osClient.objectContent = "test-content".getBytes(StandardCharsets.UTF_8);

        OracleStorageResource oracleStorageResource = new OracleStorageResource("testBucket", "testObject", osClient);
        try (InputStream inputStream = oracleStorageResource.getInputStream()) {
            assertEquals("test-content", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
        assertEquals("testBucket", osClient.lastGetObjectRequest.getBucketName());
        assertEquals("testObject", osClient.lastGetObjectRequest.getObjectName());
    }

    @Test
    void testGetDescription() {
        OracleStorageResource oracleStorageResource =
                new OracleStorageResource("testBucket", "testObject", StorageTestSupport.newObjectStorageClient());
        assertNotNull(oracleStorageResource.getDescription());
    }

    @Test
    void testWritableResourceUploadsOnClose() throws Exception {
        StorageTestSupport.FakeObjectStorageClient osClient = StorageTestSupport.newObjectStorageClient();
        StorageTestSupport.RecordingContentTypeResolver contentTypeResolver =
                new StorageTestSupport.RecordingContentTypeResolver("text/plain");
        StorageTestSupport.RecordingStorageObjectUploader objectUploader =
                new StorageTestSupport.RecordingStorageObjectUploader();
        OracleStorageResource oracleStorageResource =
                new OracleStorageResource("testBucket", "testObject", osClient, contentTypeResolver, objectUploader);

        assertInstanceOf(WritableResource.class, oracleStorageResource);
        assertTrue(oracleStorageResource.isWritable());

        try (OutputStream outputStream = oracleStorageResource.getOutputStream()) {
            outputStream.write("sample".getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(1, objectUploader.uploadCount);
        assertEquals("sample", new String(objectUploader.lastContent, StandardCharsets.UTF_8));
        assertEquals("text/plain", objectUploader.lastRequest.getContentType());
        assertEquals("testObject", contentTypeResolver.lastObjectName);
    }
}
