/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.WritableResource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Default OCI Storage resource implementation of Spring Resource.
 */
public class OracleStorageResource extends AbstractResource implements WritableResource {

    private final ObjectStorageClient osClient;
    private final StorageLocation location;
    @Nullable
    private final StorageContentTypeResolver contentTypeResolver;
    private final StorageObjectUploader objectUploader;

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
        this(new StorageLocation(bucketName, objectName), osClient, null, new UploadManagerStorageObjectUploader());
    }

    OracleStorageResource(String bucketName, String objectName,
                          ObjectStorageClient osClient,
                          @Nullable StorageContentTypeResolver contentTypeResolver) {
        this(new StorageLocation(bucketName, objectName), osClient, contentTypeResolver,
                new UploadManagerStorageObjectUploader());
    }

    OracleStorageResource(String bucketName, String objectName,
                          ObjectStorageClient osClient,
                          @Nullable StorageContentTypeResolver contentTypeResolver,
                          StorageObjectUploader objectUploader) {
        this(new StorageLocation(bucketName, objectName), osClient, contentTypeResolver, objectUploader);
    }

    public OracleStorageResource(StorageLocation location, ObjectStorageClient osClient) {
        this(location, osClient, null, new UploadManagerStorageObjectUploader());
    }

    OracleStorageResource(StorageLocation location, ObjectStorageClient osClient,
                          @Nullable StorageContentTypeResolver contentTypeResolver) {
        this(location, osClient, contentTypeResolver, new UploadManagerStorageObjectUploader());
    }

    OracleStorageResource(StorageLocation location, ObjectStorageClient osClient,
                          @Nullable StorageContentTypeResolver contentTypeResolver,
                          StorageObjectUploader objectUploader) {
        this.location = location;
        this.osClient = osClient;
        this.contentTypeResolver = contentTypeResolver;
        this.objectUploader = objectUploader;
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

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public OutputStream getOutputStream() {
        return new UploadOnCloseOutputStream();
    }

    OracleStorageResource upload(InputStream inputStream, @Nullable StorageObjectMetadata objectMetadata) throws IOException {
        Assert.notNull(inputStream, "inputStream is required");

        Path temporaryFile = Files.createTempFile("oci-storage-resource-", ".tmp");

        try {
            long contentLength;
            try (InputStream source = inputStream;
                 OutputStream fileOutputStream = Files.newOutputStream(temporaryFile)) {
                contentLength = source.transferTo(fileOutputStream);
            }

            PutObjectRequest.Builder builder = PutObjectRequest.builder()
                    .bucketName(location.getBucket())
                    .namespaceName(getNamespaceName())
                    .objectName(location.getObject())
                    .contentLength(resolveContentLength(contentLength, objectMetadata));

            if (objectMetadata != null) {
                objectMetadata.apply(builder);
            }

            String contentType = resolveContentType(objectMetadata);
            if (contentType != null) {
                builder.contentType(contentType);
            }

            PutObjectRequest putObjectRequest = builder.build();
            objectUploader.upload(osClient, putObjectRequest, temporaryFile);
            return this;
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private String getNamespaceName() {
        GetNamespaceResponse namespaceResponse =
                osClient.getNamespace(GetNamespaceRequest.builder().build());
        return namespaceResponse.getValue();
    }

    private long resolveContentLength(long contentLength, @Nullable StorageObjectMetadata objectMetadata) {
        if (objectMetadata != null && objectMetadata.getContentLength() != null) {
            return objectMetadata.getContentLength();
        }
        return contentLength;
    }

    @Nullable
    private String resolveContentType(@Nullable StorageObjectMetadata objectMetadata) {
        if (objectMetadata != null && objectMetadata.getContentType() != null) {
            return objectMetadata.getContentType();
        }
        if (contentTypeResolver != null) {
            return contentTypeResolver.resolveContentType(location.getObject());
        }
        return null;
    }

    private final class UploadOnCloseOutputStream extends ByteArrayOutputStream {
        private boolean closed;

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }

            closed = true;
            super.close();

            StorageObjectMetadata metadata = StorageObjectMetadata.builder()
                    .contentLength((long) size())
                    .build();
            upload(new ByteArrayInputStream(toByteArray()), metadata);
        }
    }
}
