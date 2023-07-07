/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.s3.springcloudocis3sample;

import com.oracle.cloud.spring.storage.OracleStorageResource;
import com.oracle.cloud.spring.storage.Storage;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Scanner;

@RestController
@RequestMapping("demoapp/api/object")
@Tag(name="Storage Object APIs")
public class ObjectController {

    @Autowired
    Storage storage;

    @Autowired
    ResourceLoader loader;

    @Value("ocs://your-bucket/555.json")
    Object myObject;

    @GetMapping("/")
    String hello() throws IOException {
        Object obj = loader.getResource("ocs://your-bucket/555.json");
        Scanner s = new Scanner(((OracleStorageResource)obj).getInputStream()).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        System.out.println(result);
        return "Hello World ";
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
