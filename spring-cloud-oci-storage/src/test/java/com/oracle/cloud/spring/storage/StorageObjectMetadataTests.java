/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.objectstorage.model.StorageTier;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.retrier.RetryConfiguration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class StorageObjectMetadataTests {
    @Test
    public void testStorageObjectMetadata() {
        StorageObjectMetadata storageObjectMetadata = new StorageObjectMetadata();
        storageObjectMetadata.setContentEncoding("UTF-8");
        assertEquals("UTF-8", storageObjectMetadata.getContentEncoding());
        storageObjectMetadata.setContentLanguage("EN");
        assertEquals("EN", storageObjectMetadata.getContentLanguage());
        storageObjectMetadata.setContentType("application/json");
        assertEquals("application/json", storageObjectMetadata.getContentType());
        storageObjectMetadata.setCacheControl("true");
        assertEquals("true", storageObjectMetadata.getCacheControl());
        storageObjectMetadata.setContentLength(500L);
        assertEquals(500L, storageObjectMetadata.getContentLength());
        storageObjectMetadata.setContentMD5("abc");
        assertEquals("abc", storageObjectMetadata.getContentMD5());
        storageObjectMetadata.setExpect("result");
        assertEquals("result", storageObjectMetadata.getExpect());
        storageObjectMetadata.setIfMatch("true");
        assertEquals("true", storageObjectMetadata.getIfMatch());
        storageObjectMetadata.setIfNoneMatch("true");
        assertEquals("true", storageObjectMetadata.getIfNoneMatch());
        storageObjectMetadata.setRetryConfiguration(new RetryConfiguration.Builder().build());
        assertNotNull(storageObjectMetadata.getRetryConfiguration());
        storageObjectMetadata.setStorageTier(StorageTier.Standard);
        assertEquals(StorageTier.Standard, (storageObjectMetadata.getStorageTier()));
        storageObjectMetadata.setOpcMeta(new HashMap<>());
        assertNotNull(storageObjectMetadata.getOpcMeta());
        storageObjectMetadata.setContentDisposition("true");
        assertEquals("true", storageObjectMetadata.getContentDisposition());
        storageObjectMetadata.setOpcClientRequestId("opcRequestId");
        assertEquals("opcRequestId", storageObjectMetadata.getOpcClientRequestId());
        storageObjectMetadata.setOpcSseCustomerAlgorithm("algo");
        assertEquals("algo", storageObjectMetadata.getOpcSseCustomerAlgorithm());
        storageObjectMetadata.setOpcSseCustomerKey("key");
        assertEquals("key", storageObjectMetadata.getOpcSseCustomerKey());
        storageObjectMetadata.setOpcSseKmsKeyId("keyId");
        assertEquals("keyId", storageObjectMetadata.getOpcSseKmsKeyId());
        storageObjectMetadata.setOpcSseCustomerKeySha256("keySha256");
        assertEquals("keySha256", storageObjectMetadata.getOpcSseCustomerKeySha256());

        storageObjectMetadata.apply(PutObjectRequest.builder()
                .bucketName("testBucket")
                .namespaceName("testNS")
                .objectName("testKey"));
    }

    @Test
    public void testStorageObjectMetadataBuilder() {
        StorageObjectMetadata.Builder builder = StorageObjectMetadata.builder()
                .contentEncoding("UTF-8")
                .contentLanguage("EN")
                .contentType("application/json")
                .cacheControl("true")
                .contentDisposition("true")
                .contentLength(500L)
                .contentMD5("abc")
                .expect("result")
                .ifMatch("true")
                .ifNoneMatch("true")
                .retryConfiguration(new RetryConfiguration.Builder().build())
                .storageTier(StorageTier.Standard)
                .metadata("key", "value")
                .opcClientRequestId("requestId")
                .opcSseKmsKeyId("keyId")
                .opcSseCustomerKey("key")
                .opcSseCustomerAlgorithm("algo")
                .opcSseCustomerKeySha256("keySha256");

        builder.build().apply(PutObjectRequest.builder()
                .bucketName("testBucket")
                .namespaceName("testNS")
                .objectName("testKey"));
    }
}
