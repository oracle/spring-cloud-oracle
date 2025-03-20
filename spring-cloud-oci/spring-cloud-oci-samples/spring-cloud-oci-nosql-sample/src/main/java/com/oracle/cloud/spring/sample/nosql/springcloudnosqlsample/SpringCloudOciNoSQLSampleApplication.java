// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.sample.nosql.springcloudnosqlsample;

import com.oracle.nosql.spring.data.config.AbstractNosqlConfiguration;
import com.oracle.nosql.spring.data.repository.config.EnableNosqlRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableNosqlRepositories
public class SpringCloudOciNoSQLSampleApplication extends AbstractNosqlConfiguration {
    public static void main(String[] args) {
        SpringApplication.run(SpringCloudOciNoSQLSampleApplication.class, args);
    }
}
