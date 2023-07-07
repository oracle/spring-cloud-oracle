/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.s3.springcloudocis3sample;

import com.oracle.cloud.spring.storage.Storage;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demoapp/api/bucket")
@Tag(name = "Storage Bucket APIs")
public class BucketController {

    @Autowired
    Storage storage;

    @PostMapping("/")
    void createBucket(@Parameter(required = true, example = "new-bucket") @RequestParam String bucketName,
                      @Parameter(required = false) @RequestParam(required = false) String compartmentId) {
        if (compartmentId != null) {
            storage.createBucket(bucketName, compartmentId);
        } else {
            storage.createBucket(bucketName);
        }
    }

    @DeleteMapping("/{bucketName}")
    void deleteBucket(@Parameter(required = true, example = "new-bucket") @PathVariable String bucketName) {
        storage.deleteBucket(bucketName);
    }
}
