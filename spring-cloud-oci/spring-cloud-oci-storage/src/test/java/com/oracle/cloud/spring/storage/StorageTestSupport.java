/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.CreateBucketRequest;
import com.oracle.bmc.objectstorage.requests.DeleteBucketRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreateBucketResponse;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

final class StorageTestSupport {

    private StorageTestSupport() {
    }

    static FakeObjectStorageClient newObjectStorageClient() {
        return new FakeObjectStorageClient();
    }

    static final class FakeObjectStorageClient extends ObjectStorageClient {
        private static final String PRIVATE_KEY = """
                -----BEGIN PRIVATE KEY-----
                MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMg7GKI/0sbjirpT
                2UPcabmjDbWfO1ZsvLbIq6PgPVaW443yFFM9fYxUBS6a3MFx1rsOD0sSzWl1LsZx
                Wc0bxi7KdQ+bjdZ5UBPwzrtxXDyp+ufWnv7yecV/TwmvHTqeaL+l5VbBMYXuiQnv
                yhaLL10zMlEEvMk3GbQZVJ9DMs5nAgMBAAECgYEAglL0laY06n7vrJcmsqSjq9AU
                /EHHvVjI+69hCCjLw7AyLBGEaSl8rfmB5fOl+8K8oMNl8NcsG5fJ+h+M85NASczi
                2GStDOZ4OI90f+B3/YPgCWfL26g/xe6cW2/oURHC1btDY04x7vw1W++6CB+XXfUD
                GBbm/pHRv3qRxpBxxgECQQDxqHlg1F8v1d4DzqVTkIFH3oWbJVb2MVz/MoXuCdBA
                yvyzVGu2JotNvAgPV66zW+6/bWk07GiRj6eeHOJuDKDvAkEA1B03HthHu+hCjgzc
                kXUhYrOjSiKjQXKS21IJJ2o3H9lxh9Qkvnr1vfdOFttQEUBWXza2G3yEBs9GNr69
                lmU6CQJAGIkgecJWP8cZGY3bn1ZmqeNf8VajM6/jX03D5107tbhmW9bQcNgNAMF8
                mAIxDKji3rC/I85094J8ZENOghnqJQJAIibyEQ1Rv3eN/8EiYmkxjurNh8o77vW7
                n4R95NK9PWuNVAlcQS8bEhMXh6aYJa7uOTZd698IgvAspfPgIq75wQJBAOkx78wE
                vOOmAEsvjjR5PfKoIA4wYDYocUcrC9Fz51pGc8dftoNR/6ml7/PNbuyS3oA4ZjPU
                2a62fTbqYKvhyoc=
                -----END PRIVATE KEY-----
                """;

        String namespace = "testNamespace";
        byte[] objectContent = "sample".getBytes(StandardCharsets.UTF_8);
        CreateBucketRequest lastCreateBucketRequest;
        DeleteBucketRequest lastDeleteBucketRequest;
        DeleteObjectRequest lastDeleteObjectRequest;
        GetObjectRequest lastGetObjectRequest;

        FakeObjectStorageClient() {
            super(new TestAuthenticationDetailsProvider());
        }

        @Override
        public GetNamespaceResponse getNamespace(GetNamespaceRequest request) {
            return GetNamespaceResponse.builder()
                    .__httpStatusCode__(200)
                    .headers(Map.of())
                    .value(namespace)
                    .build();
        }

        @Override
        public GetObjectResponse getObject(GetObjectRequest request) {
            lastGetObjectRequest = request;
            return GetObjectResponse.builder()
                    .__httpStatusCode__(200)
                    .headers(Map.of())
                    .contentLength((long) objectContent.length)
                    .inputStream(new ByteArrayInputStream(objectContent))
                    .build();
        }

        @Override
        public CreateBucketResponse createBucket(CreateBucketRequest request) {
            lastCreateBucketRequest = request;
            return CreateBucketResponse.builder()
                    .__httpStatusCode__(200)
                    .headers(Map.of())
                    .location("/b/" + request.getCreateBucketDetails().getName())
                    .build();
        }

        @Override
        public com.oracle.bmc.objectstorage.responses.DeleteBucketResponse deleteBucket(DeleteBucketRequest request) {
            lastDeleteBucketRequest = request;
            return null;
        }

        @Override
        public com.oracle.bmc.objectstorage.responses.DeleteObjectResponse deleteObject(DeleteObjectRequest request) {
            lastDeleteObjectRequest = request;
            return null;
        }

        private static final class TestAuthenticationDetailsProvider implements BasicAuthenticationDetailsProvider {

            @Override
            public String getKeyId() {
                return "ocid1.tenancy.oc1..test/ocid1.user.oc1..test/fingerprint";
            }

            @Override
            public InputStream getPrivateKey() {
                return new ByteArrayInputStream(PRIVATE_KEY.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public String getPassPhrase() {
                return null;
            }

            @Override
            public char[] getPassphraseCharacters() {
                return null;
            }
        }
    }

    static final class RecordingStorageObjectUploader implements StorageObjectUploader {
        int uploadCount;
        PutObjectRequest lastRequest;
        byte[] lastContent = new byte[0];

        @Override
        public void upload(ObjectStorageClient osClient, PutObjectRequest putObjectRequest, Path sourceFile) throws IOException {
            uploadCount++;
            lastRequest = putObjectRequest;
            lastContent = Files.readAllBytes(sourceFile);
        }
    }

    static final class RecordingContentTypeResolver implements StorageContentTypeResolver {
        private final String contentType;
        String lastObjectName;

        RecordingContentTypeResolver(String contentType) {
            this.contentType = contentType;
        }

        @Override
        public String resolveContentType(String fileName) {
            lastObjectName = fileName;
            return contentType;
        }
    }

    static final class RecordingStorageObjectConverter implements StorageObjectConverter {
        byte[] writeBytes = "sample".getBytes(StandardCharsets.UTF_8);
        RuntimeException readException;
        Object readValue = "value";
        Object lastWrittenObject;

        @Override
        public <T> byte[] write(T object) {
            lastWrittenObject = object;
            return writeBytes;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T read(InputStream is, Class<T> clazz) {
            if (readException != null) {
                throw readException;
            }
            return (T) readValue;
        }

        @Override
        public String contentType() {
            return "application/json";
        }
    }
}
