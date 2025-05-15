// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityViewScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {
        "com.oracle.spring.json.duality.model"
    }
)
@JsonRelationalDualityViewScan(
        basePackages = {
            "com.oracle.spring.json.duality.model"
        }
)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
