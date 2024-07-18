/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import org.springframework.core.io.AbstractResource;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Default OCI Storage resource implementation of Spring Resource.
 */
public class OracleStorageResource extends AbstractResource {

    private final ObjectStorageClient osClient;
    private final StorageLocation location;

    /**
     * Creates new OracleStorageResource identified by location URI and other information.
     * @param location Object URI
     * @param osClient OCI Storage SDK Client instance
     * @return OracleStorageResource
     */
    @Nullable
    public static OracleStorageResource create(String location, ObjectStorageClient osClient) {
        StorageLocation locationObject = StorageLocation.resolve(location);
        if (locationObject != null) {
            return new OracleStorageResource(locationObject, osClient);
        }

        return null;
    }

    public OracleStorageResource(String bucketName, String objectName,
                                 ObjectStorageClient osClient) {
        this(new StorageLocation(bucketName, objectName), osClient);
    }

    public OracleStorageResource(StorageLocation location, ObjectStorageClient osClient) {
        this.location = location;
        this.osClient = osClient;
    }

    /**
     * Gets the description of the resource.
     * @return String
     */
    @Override
    public String getDescription() {
        return location.toString();
    }

    /**
     * Get the Input Stream instance for the given Storage Resource.
     * @return InputStream
     * @throws IOException
     */
    @Override
    public InputStream getInputStream() throws IOException {
        GetNamespaceResponse namespaceResponse =
                osClient.getNamespace(GetNamespaceRequest.builder().build());
        String namespaceName = namespaceResponse.getValue();

        GetObjectResponse getResponse =
                osClient.getObject(
                        GetObjectRequest.builder()
                                .namespaceName(namespaceName)
                                .bucketName(location.getBucket())
                                .objectName(location.getObject())
                                .build());
        return getResponse.getInputStream();
    }
}
