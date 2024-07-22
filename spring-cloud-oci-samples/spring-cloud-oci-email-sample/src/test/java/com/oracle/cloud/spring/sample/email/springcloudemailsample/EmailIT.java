package com.oracle.cloud.spring.sample.email.springcloudemailsample;

import java.io.File;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OCI_SMTP_HOST", matches = "+.")
@EnabledIfEnvironmentVariable(named = "OCI_SMTP_USERNAME", matches = "+.")
@EnabledIfEnvironmentVariable(named = "OCI_SMTP_PASSWORD", matches = "+.")
@EnabledIfEnvironmentVariable(named = "OCI_FROM_ADDRESS", matches = "+.")
public class EmailIT {
    @Autowired
    EmailService emailService;

    private final String address = System.getenv("OCI_FROM_ADDRESS");

    @Test
    void sendJavaMail() throws Exception {
        File attachmentFile = new File("src/test/resources/attachment.txt");
        emailService.sendJavaMail(
                address,
                address,
                "Java Mail Test",
                "This is a test of the EmailDeliveryJavaMailSender Bean",
                attachmentFile
        );
    }

    @Test
    @Disabled
    void sendSimpleMail() {
        emailService.sendSimpleMail(
                address,
                address,
                "Simple Email Test",
                "This is a test of the EmailDeliveryMailSender Bean"
        );
    }
}
