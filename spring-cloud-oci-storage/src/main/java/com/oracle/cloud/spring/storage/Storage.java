/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.responses.CreateBucketResponse;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for defining OCI storage module
 */
public interface Storage {

    /**
     * Download latest version of specific object from OCI Object Storage.
     * @param bucketName OCI storage bucket name.
     * @param key Object name
     * @return {@link com.oracle.cloud.spring.storage.OracleStorageResource}
     */
    OracleStorageResource download(String bucketName, String key);

    /**
     * Download specific object from OCI Object Storage.
     * @param bucketName OCI storage bucket name.
     * @param key Object name
     * @param version Version of the object
     * @return {@link com.oracle.cloud.spring.storage.OracleStorageResource}
     */
    OracleStorageResource download(String bucketName, String key, String version);

    /**
     * Upload new object (via InputStream) to OCI Object Storage.
     * @param bucketName OCI storage bucket name.
     * @param key Object name
     * @param inputStream Object data with InputStream data type.
     * @param objectMetadata {@link com.oracle.cloud.spring.storage.StorageObjectMetadata}
     * @return {@link com.oracle.cloud.spring.storage.OracleStorageResource}
     */
    OracleStorageResource upload(String bucketName, String key, InputStream inputStream,
                                 @Nullable StorageObjectMetadata objectMetadata) throws IOException;

    /**
     * Upload new object (via InputStream) to OCI Object Storage with default metadata.
     * @param bucketName OCI storage bucket name.
     * @param key Object name
     * @param inputStream Object data with InputStream data type.
     * @return {@link com.oracle.cloud.spring.storage.OracleStorageResource}
     */
    default OracleStorageResource upload(String bucketName, String key, InputStream inputStream) throws IOException {
        return upload(bucketName, key, inputStream, null);
    }

    /**
     * Upload Java POJO as JSON object.
     * @param bucketName OCI storage bucket name.
     * @param key Object name
     * @param object POJO object to be stored as json.
     * @return {@link com.oracle.cloud.spring.storage.OracleStorageResource}
     */
    OracleStorageResource store(String bucketName, String key, Object object) throws IOException;

    /**
     * Read JSON file stored on Object storage and convert to Java POJO.
     * @param bucketName OCI storage bucket name.
     * @param key Object name
     * @param clazz Type of the Java POJO.
     * @return Object instnace of clazz.
     */
    <T> T read(String bucketName, String key, Class<T> clazz);

    /**
     * Direct instance of OCI Java SDK Storage Client.
     * @return ObjectStorageClient
     */
    ObjectStorageClient getClient();

    /**
     * Create new bucket with the specified bucket name.
     * @param bucketName OCI storage bucket name.
     * @return CreateBucketResponse
     */
    CreateBucketResponse createBucket(String bucketName);

    /**
     * Create new bucket with the specified bucket name on a specific OCI compartment.
     * @param bucketName OCI storage bucket name.
     * @param compartmentId OCI compartment OCID.
     * @return CreateBucketResponse
     */
    CreateBucketResponse createBucket(String bucketName, String compartmentId);

    /**
     * Delete storage bucket.
     * @param bucketName OCI storage bucket name.
     */
    void deleteBucket(String bucketName);

    /**
     * Delete storage object based on bucket name and object key.
     * @param bucketName OCI storage bucket name.
     * @param key Object name/key.s
     */
    void deleteObject(String bucketName, String key);

    /**
     * Get the current OCI storage namespace.
     * @return name of the namespace.
     */
    String getNamespaceName();
}
