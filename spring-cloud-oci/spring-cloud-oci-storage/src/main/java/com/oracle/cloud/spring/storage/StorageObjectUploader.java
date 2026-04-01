/*
 ** Copyright (c) 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

interface StorageObjectUploader {

    void upload(ObjectStorageClient osClient, PutObjectRequest putObjectRequest, Path sourceFile) throws IOException;
}

final class UploadManagerStorageObjectUploader implements StorageObjectUploader {

    @Override
    public void upload(ObjectStorageClient osClient, PutObjectRequest putObjectRequest, Path sourceFile) throws IOException {
        UploadConfiguration uploadConfiguration =
                UploadConfiguration.builder()
                        .allowMultipartUploads(true)
                        .allowParallelUploads(true)
                        .build();
        UploadManager uploadManager = new UploadManager(osClient, uploadConfiguration);

        try (InputStream uploadStream = Files.newInputStream(sourceFile)) {
            UploadManager.UploadRequest uploadRequest =
                    UploadManager.UploadRequest.builder(uploadStream, putObjectRequest.getContentLength())
                            .build(putObjectRequest);
            uploadManager.upload(uploadRequest);
        }
    }
}
