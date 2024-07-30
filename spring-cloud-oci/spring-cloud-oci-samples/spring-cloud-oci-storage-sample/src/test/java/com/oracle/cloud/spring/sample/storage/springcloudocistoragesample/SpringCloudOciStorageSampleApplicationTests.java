/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.storage.springcloudocistoragesample;

import com.oracle.cloud.spring.storage.Storage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OCI_NAMESPACE", matches = ".+")
@EnabledIfEnvironmentVariable(named = "OCI_BUCKET", matches = ".+")
@EnabledIfEnvironmentVariable(named = "OCI_OBJECT", matches = ".+")
@EnabledIfEnvironmentVariable(named = "OCI_COMPARTMNET", matches = ".+")
class SpringCloudOciStorageSampleApplicationTests {

    static final String testBucket = System.getenv("OCI_BUCKET");

    @Autowired
    Storage storage;

    @Autowired
    ObjectController objectController;

    @Test
    void resourceIsLoaded() throws IOException {
        Resource myObject = objectController.myObject;
        assertThat(myObject).isNotNull();
        assertThat(myObject.getContentAsByteArray()).hasSizeGreaterThan(1);
    }

    @Test
    @Disabled
    void storePOJO() throws IOException {
        ActivityInfo ainfo = new ActivityInfo("Hello from Storage integration test");
        storage.store(testBucket, ainfo.getFileName(), ainfo);
    }

    private class ActivityInfo {
        long time = System.currentTimeMillis();
        String message;

        public ActivityInfo(String message) { this.message = message; }

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


