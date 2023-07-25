/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.email;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringCloudOciEmailSampleApplicationTests {

    @BeforeAll
    static void beforeAll() {
        System.setProperty("MAIL_SMTP_HOST", "dummySmtpHost");
        System.setProperty("MAIL_SMTP_USER", "dummySmtpUser");
    }

    @Test
    void contextLoads() {

    }

}
