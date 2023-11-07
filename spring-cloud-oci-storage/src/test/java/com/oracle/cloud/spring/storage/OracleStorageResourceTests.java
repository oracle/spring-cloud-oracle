/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

public class OracleStorageResourceTests {
    final ObjectStorageClient osClient = mock(ObjectStorageClient.class);
    final OracleStorageResource oracleStorageResource = new OracleStorageResource("testBucket", "testObject", "1.0", osClient);

    @Test
    public void testCreate() {
        try (MockedStatic mock = mockStatic(StorageLocation.class)) {
            OracleStorageResource.create("ocs://test/bucket", osClient);
        }
    }

    @Test
    public void testGetInputStream() throws Exception {
        when(osClient.getNamespace(any())).thenReturn(mock(GetNamespaceResponse.class));
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(osClient.getObject(any())).thenReturn(mockResponse);
        when(mockResponse.getInputStream()).thenReturn(mock(InputStream.class));
        assertNotNull(oracleStorageResource.getInputStream());
    }

    @Test
    public void testGetDescription() {
        assertNotNull(oracleStorageResource.getDescription());
    }
}
