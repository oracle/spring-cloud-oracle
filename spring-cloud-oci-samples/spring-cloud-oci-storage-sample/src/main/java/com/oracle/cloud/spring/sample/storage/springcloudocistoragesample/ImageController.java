/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.storage.springcloudocistoragesample;

import com.oracle.cloud.spring.storage.Storage;
import com.oracle.cloud.spring.storage.StorageObjectMetadata;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("demoapp/api/image")
@Tag(name = "Image Object APIs")
public class ImageController {

    @Autowired
    Storage storage;

    @GetMapping("/{bucketName}/{objectName}")
    ResponseEntity<Resource> download(@Parameter(required = true, example = "new-bucket") @PathVariable String bucketName,
                                      @Parameter(required = true) @PathVariable String objectName,
                                      @Parameter(required = false, example = "image/jpeg") @RequestParam(required = false) String mediaType) {
        MediaType mt = APPLICATION_OCTET_STREAM;
        if (mediaType != null && MediaType.valueOf(mediaType) != null) {
           mt = MediaType.valueOf(mediaType);
        }

        return ResponseEntity.ok()
                .contentType(mt)
                .body(storage.download(bucketName, objectName));
    }

    @PostMapping(value = "/{bucketName}", consumes = MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> upload(@Parameter(required = true) @RequestPart(required = true, name = "file") MultipartFile multipartFile,
                             @Parameter(required = true, example = "new-bucket") @PathVariable String bucketName) throws IOException {
        //if (!IMAGE_JPEG.toString().equals(multipartFile.getContentType())) {
          //  return ResponseEntity.badRequest().body("Invalid image file");
       // }

        try (InputStream is = multipartFile.getInputStream()) {
            storage.upload(bucketName, multipartFile.getOriginalFilename(), is,
                    StorageObjectMetadata.builder().contentType(multipartFile.getContentType()).build());
        }

        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{bucketName}/{objectName}")
    void deleteObject(@Parameter(required = true, example = "new-bucket") @PathVariable String bucketName,
                      @Parameter(required = true) @PathVariable String objectName) {
        storage.deleteObject(bucketName, objectName);
    }

}
