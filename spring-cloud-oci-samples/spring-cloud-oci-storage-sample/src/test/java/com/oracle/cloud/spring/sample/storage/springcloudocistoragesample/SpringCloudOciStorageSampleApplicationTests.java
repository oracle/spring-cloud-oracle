/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.storage.springcloudocistoragesample;

import com.oracle.cloud.spring.sample.common.base.SpringCloudSampleApplicationTestBase;
import com.oracle.cloud.spring.storage.Storage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

@SpringBootTest
@EnabledIfSystemProperty(named = "it.storage", matches = "true")
@TestPropertySource(locations="classpath:application-test.properties")
class SpringCloudOciStorageSampleApplicationTests extends SpringCloudSampleApplicationTestBase {
    static final String TEST_BUCKET = "bucketName";

    static final String testBucket = System.getProperty(TEST_BUCKET) != null ? System.getProperty(TEST_BUCKET) :
            System.getenv().get(TEST_BUCKET);

    @Autowired
    Storage storage;

    @Test
    void testFileUpload() throws IOException {
        ActivityInfo ainfo = new ActivityInfo("Hello from Storage integration test");
        storage.store(testBucket, ainfo.getFileName(), ainfo);
    }

    private class ActivityInfo {
        long time = System.currentTimeMillis();
        String message;

        ActivityInfo(String message) { this.message = message; }

        String getFileName() {
            return "activity_" + time + ".json";
        }

        long getTime() {
            return time;
        }

        String getMessage() {
            return message;
        }
    }
}


