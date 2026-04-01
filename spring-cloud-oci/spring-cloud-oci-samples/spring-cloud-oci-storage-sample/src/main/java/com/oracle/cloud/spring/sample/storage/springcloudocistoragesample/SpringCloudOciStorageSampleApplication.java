/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.storage.springcloudocistoragesample;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;

@SpringBootApplication
public class SpringCloudOciStorageSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudOciStorageSampleApplication.class, args);
    }

    @Bean
    @Qualifier("sampleObjectResource")
    Resource sampleObjectResource(ResourceLoader resourceLoader,
                                  @Value("${OCI_NAMESPACE}") String namespace,
                                  @Value("${OCI_BUCKET}") String bucket,
                                  @Value("${OCI_OBJECT}") String objectName) {
        return resourceLoader.getResource(storageLocation(namespace, bucket, objectName));
    }

    @Bean
    @Qualifier("sampleWritableObjectResource")
    WritableResource sampleWritableObjectResource(@Qualifier("sampleObjectResource") Resource resource) {
        if (resource instanceof WritableResource writableResource) {
            return writableResource;
        }

        throw new IllegalStateException("OCI storage resource does not implement WritableResource");
    }

    private static String storageLocation(String namespace, String bucket, String objectName) {
        return "https://objectstorage.us-chicago-1.oraclecloud.com/n/%s/b/%s/o/%s"
                .formatted(namespace, bucket, objectName);
    }
}
