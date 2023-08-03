/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreateBucketDetails;
import com.oracle.bmc.objectstorage.requests.CreateBucketRequest;
import com.oracle.bmc.objectstorage.requests.DeleteBucketRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreateBucketResponse;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Default implementation for {@link com.oracle.cloud.spring.storage.Storage}
 */
public class StorageImpl implements Storage {
    private final ObjectStorageClient osClient;
    private final StorageObjectConverter storageObjectConverter;
    private final StorageContentTypeResolver contentTypeResolver;
    private final String defaultCompartmentOCID;

    public StorageImpl(
            ObjectStorageClient osClient,
            StorageObjectConverter storageObjectConverter,
            StorageContentTypeResolver contentTypeResolver,
            String defaultCompartmentOCID) {
        Assert.notNull(osClient, "ObjectStorageClient is required");
        Assert.notNull(storageObjectConverter, "storageObjectConverter is required");
        Assert.notNull(contentTypeResolver, "contentTypeResolver is required");

        this.osClient = osClient;
        this.storageObjectConverter = storageObjectConverter;
        this.contentTypeResolver = contentTypeResolver;
        this.defaultCompartmentOCID = defaultCompartmentOCID;
    }

    /**
     * Download specific object from OCI Object Storage.
     * @param bucketName OCI storage bucket name.
     * @param key Object name
     * @param version Version of the object
     * @return {@link com.oracle.cloud.spring.storage.OracleStorageResource}
     */
    @Override
    public OracleStorageResource download(String bucketName, String key, String version) {
        Assert.notNull(bucketName, "bucketName is required");
        Assert.notNull(key, "key is required");

        return new OracleStorageResource(bucketName, key, version, osClient);
    }

    /**
     * Download latest version of specific object from OCI Object Storage.
     * @param bucketName OCI storage bucket name.
     * @param key Object name
     * @return {@link com.oracle.cloud.spring.storage.OracleStorageResource}
     */
    @Override
    public OracleStorageResource download(String bucketName, String key) {
        return download(bucketName, key, null);
    }

    /**
     * Upload new object (via InputStream) to OCI Object Storage.
     * @param bucketName OCI storage bucket name.
     * @param key Object name
     * @param inputStream Object data with InputStream data type.
     * @param objectMetadata {@link com.oracle.cloud.spring.storage.StorageObjectMetadata}
     * @return {@link com.oracle.cloud.spring.storage.OracleStorageResource}
     */
    @Override
    public OracleStorageResource upload(String bucketName, String key, InputStream inputStream,
                                        @Nullable StorageObjectMetadata objectMetadata) throws IOException {
        Assert.notNull(bucketName, "bucketName is required");
        Assert.notNull(key, "key is required");
        Assert.notNull(inputStream, "inputStream is required");

        UploadConfiguration uploadConfiguration =
                UploadConfiguration.builder()
                        .allowMultipartUploads(true)
                        .allowParallelUploads(true)
                        .build();
        UploadManager uploadManager = new UploadManager(osClient, uploadConfiguration);

        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucketName(bucketName)
                .namespaceName(getNamespaceName())
                .objectName(key);

        if (objectMetadata != null) {
            objectMetadata.apply(builder);
        }

        builder.contentType(resolveContentType(key, objectMetadata));
        PutObjectRequest putObjectRequest = builder.build();

        UploadManager.UploadRequest uploadRequest = UploadManager.UploadRequest.builder(inputStream, inputStream.available()).build(putObjectRequest);
        UploadManager.UploadResponse uploadResponse = uploadManager.upload(uploadRequest);
        System.out.println(uploadResponse);

        return null;
    }

    /**
     * Upload Java POJO as JSON object.
     * @param bucketName OCI storage bucket name.
     * @param key Object name
     * @param object POJO object to be stored as json.
     * @return {@link com.oracle.cloud.spring.storage.OracleStorageResource}
     */
    @Override
    public OracleStorageResource store(String bucketName, String key, Object object) throws IOException {
        Assert.notNull(bucketName, "bucketName is required");
        Assert.notNull(key, "key is required");
        Assert.notNull(object, "object is required");

        return upload(bucketName, key, new ByteArrayInputStream(storageObjectConverter.write(object)), null);
    }

    /**
     * Read JSON file stored on Object storage and convert to Java POJO.
     * @param bucketName OCI storage bucket name.
     * @param key Object name
     * @param clazz Type of the Java POJO.
     * @return Object instnace of clazz.
     */
    @Override
    public <T> T read(String bucketName, String key, Class<T> clazz) {
        Assert.notNull(bucketName, "bucketName is required");
        Assert.notNull(key, "key is required");
        Assert.notNull(clazz, "clazz is required");

        try {
            return storageObjectConverter.read(download(bucketName, key).getInputStream(), clazz);
        } catch (Exception e) {
            throw new StorageException(
                    String.format("Failed to read object with a key '%s' from bucket '%s'", key, bucketName), e);
        }
    }

    /**
     * Direct instance of OCI Java SDK Storage Client.
     * @return ObjectStorageClient
     */
    @Override
    public ObjectStorageClient getClient() {
        return osClient;
    }

    /**
     * Create new bucket with the specified bucket name.
     * @param bucketName OCI storage bucket name.
     * @return CreateBucketResponse
     */
    @Override
    public CreateBucketResponse createBucket(String bucketName) {
        return createBucket(bucketName, defaultCompartmentOCID);
    }

    /**
     * Create new bucket with the specified bucket name on a specific OCI compartment.
     * @param bucketName OCI storage bucket name.
     * @param compartmentId OCI compartment OCID.
     * @return CreateBucketResponse
     */
    @Override
    public CreateBucketResponse createBucket(String bucketName, String compartmentId) {
        Assert.notNull(bucketName, "bucketName is required");
        Assert.notNull(compartmentId, "compartmentId is required");

        String namespaceName = getNamespaceName();
        CreateBucketDetails.Builder builder = CreateBucketDetails.builder().name(bucketName);
        builder.compartmentId(compartmentId);

        CreateBucketDetails createSourceBucketDetails = builder.build();

        CreateBucketRequest createSourceBucketRequest =
                CreateBucketRequest.builder()
                        .namespaceName(namespaceName)
                        .createBucketDetails(createSourceBucketDetails)
                        .build();
        return osClient.createBucket(createSourceBucketRequest);
    }

    /**
     * Delete storage bucket.
     * @param bucketName OCI storage bucket name.
     */
    @Override
    public void deleteBucket(String bucketName) {
        Assert.notNull(bucketName, "bucketName is required");

        DeleteBucketRequest deleteBucketRequest =
                DeleteBucketRequest.builder()
                        .namespaceName(getNamespaceName())
                        .bucketName(bucketName)
                        .build();
        osClient.deleteBucket(deleteBucketRequest);
    }

    /**
     * Delete storage object based on bucket name and object key.
     * @param bucketName OCI storage bucket name.
     * @param key Object name/key.s
     */
    @Override
    public void deleteObject(String bucketName, String key) {
        Assert.notNull(bucketName, "bucketName is required");
        Assert.notNull(key, "key is required");

        DeleteObjectRequest deleteObjectRequest =
                DeleteObjectRequest.builder()
                        .objectName(key)
                        .bucketName(bucketName)
                        .namespaceName(getNamespaceName())
                        .build();

        osClient.deleteObject(deleteObjectRequest);
    }

    /**
     * Get the current OCI storage namespace.
     * @return name of the namespace.
     */
    @Override
    public String getNamespaceName() {
        GetNamespaceResponse namespaceResponse =
                osClient.getNamespace(GetNamespaceRequest.builder().build());
        return namespaceResponse.getValue();
    }

    private String resolveContentType(String objectName, StorageObjectMetadata metadata) {
        if (metadata != null && metadata.getContentType() != null) {
            return metadata.getContentType();
        }

        if (contentTypeResolver != null && (metadata == null || metadata.getContentType() == null)) {
            contentTypeResolver.resolveContentType(objectName);
        }

        return null;
    }
}
