// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package com.oracle.cloud.spring.sample.adb.springcloudociadbsample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demoapp/api")
public class HelloController {
    @GetMapping("/")
    public String hello() {
        return "Hello World From Autonomus Database Service";
    }
}
