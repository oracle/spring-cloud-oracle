/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.storage.springcloudocistoragesample;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oracle.cloud.spring.storage.Storage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OCI_NAMESPACE", matches = ".+")
@EnabledIfEnvironmentVariable(named = "OCI_BUCKET", matches = ".+")
@EnabledIfEnvironmentVariable(named = "OCI_OBJECT", matches = ".+")
@EnabledIfEnvironmentVariable(named = "OCI_COMPARTMENT", matches = ".+")
class SpringCloudOciStorageSampleApplicationTests {

    static final String testBucket = System.getenv("OCI_BUCKET");
    static final String testCompartment = System.getenv("OCI_COMPARTMENT");

    @Autowired
    Storage storage;

    @Autowired
    @Qualifier("sampleObjectResource")
    Resource sampleObjectResource;

    @Autowired
    @Qualifier("sampleWritableObjectResource")
    WritableResource sampleWritableObjectResource;

    @Test
    void resourceIsLoaded() throws IOException {
        assertThat(sampleObjectResource).isNotNull();
        assertThat(sampleObjectResource.getContentAsByteArray()).hasSizeGreaterThan(1);
    }

    @Test
    void writableResourceIsLoaded() {
        assertThat(sampleWritableObjectResource).isNotNull();
        assertThat(sampleWritableObjectResource.isWritable()).isTrue();
    }

    @Test
    void writableResourceRoundTrip() throws IOException {
        String content = Instant.now().toString();

        try (var outputStream = sampleWritableObjectResource.getOutputStream()) {
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }

        try (var inputStream = sampleObjectResource.getInputStream()) {
            String actual = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(actual).isEqualTo(content);
        }
    }

    @Test
    void storageUploadAndDownloadRoundTrip() throws IOException {
        String objectName = "storage-upload-" + Instant.now().toEpochMilli() + ".txt";
        String content = "upload-round-trip-" + Instant.now();

        try {
            assertThat(storage.upload(testBucket, objectName,
                    new java.io.ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))).isNotNull();

            try (InputStream inputStream = storage.download(testBucket, objectName).getInputStream()) {
                assertThat(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(content);
            }
        } finally {
            deleteQuietly(objectName);
        }
    }

    @Test
    void storageStoreAndReadRoundTrip() throws IOException {
        ActivityInfo activityInfo = new ActivityInfo("Hello from Storage integration test");

        try {
            assertThat(storage.store(testBucket, activityInfo.getFileName(), activityInfo)).isNotNull();

            ActivityInfo actual = storage.read(testBucket, activityInfo.getFileName(), ActivityInfo.class);
            assertThat(actual.getMessage()).isEqualTo(activityInfo.getMessage());
            assertThat(actual.getTime()).isEqualTo(activityInfo.getTime());
        } finally {
            deleteQuietly(activityInfo.getFileName());
        }
    }

    @Test
    void bucketCreateAndDeleteRoundTrip() {
        String bucketName = "storage-sample-" + Instant.now().toEpochMilli();

        try {
            assertThat(storage.createBucket(bucketName, testCompartment)).isNotNull();
        } finally {
            deleteBucketQuietly(bucketName);
        }
    }

    private void deleteQuietly(String objectName) {
        try {
            storage.deleteObject(testBucket, objectName);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to delete object " + objectName, ex);
        }
    }

    private void deleteBucketQuietly(String bucketName) {
        try {
            storage.deleteBucket(bucketName);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to delete bucket " + bucketName, ex);
        }
    }

    private static class ActivityInfo {
        long time = System.currentTimeMillis();
        String message;

        public ActivityInfo() {
        }

        public ActivityInfo(String message) { this.message = message; }

        @JsonIgnore
        public String getFileName() {
            return "activity_" + time + ".json";
        }

        public long getTime() {
            return time;
        }

        public String getMessage() {
            return message;
        }
    }
}
