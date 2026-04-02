/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.storage.springcloudocistoragesample;

import com.oracle.cloud.spring.storage.Storage;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("demoapp/api/object")
@Tag(name="Storage Object APIs")
public class ObjectController {

    @Autowired
    Storage storage;

    @Autowired
    @Qualifier("sampleObjectResource")
    Resource sampleObjectResource;

    @Autowired
    @Qualifier("sampleWritableObjectResource")
    WritableResource sampleWritableObjectResource;

    @GetMapping("/")
    String hello() {
        return "Hello World ";
    }

    @GetMapping("/resource")
    String readWithResource() throws IOException {
        try (var inputStream = sampleObjectResource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/resource")
    void writeWithWritableResource(@RequestBody String content) throws IOException {
        try (OutputStream outputStream = sampleWritableObjectResource.getOutputStream()) {
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    @PostMapping("/{bucketName}")
    void storeObject(@Parameter(required = true) @RequestBody Person p,
                     @Parameter(required = true, example = "new-bucket") @PathVariable String bucketName) throws IOException {
        storage.store(bucketName, p.id + ".json", p);
    }

    @GetMapping("/{bucketName}/{id}")
    Person readObject(@Parameter(required = true, example = "123") @PathVariable Long id,
                      @Parameter(required = true, example = "new-bucket") @PathVariable String bucketName) {
        return storage.read(bucketName, id + ".json", Person.class);
    }

    @DeleteMapping("/{bucketName}/{id}")
    void deleteObject(@Parameter(required = true, example = "new-bucket") @PathVariable String bucketName,
                      @Parameter(required = true, example = "123") @PathVariable String id) {
        storage.deleteObject(bucketName, id + ".json");
    }

    static class Person {
        @Schema(example = "123")
        private Long id;

        @Schema(example = "hello")
        private String firstName;

        @Schema(example = "world")
        private String lastName;

        public Person() {
        }

        public Person(Long id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
    }

}
