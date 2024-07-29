// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.email;

import java.util.Properties;

import jakarta.mail.Session;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;

@AutoConfiguration
@ConditionalOnClass({ EmailDeliveryMailSender.class })
@EnableConfigurationProperties(MailSenderProperties.class)
public class EmailDeliveryAutoConfiguration {
    private final MailSenderProperties properties;

    public EmailDeliveryAutoConfiguration(MailSenderProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Qualifier("ociMailSender")
    public MailSender emailDeliveryMailSender(Session session) {
        return new EmailDeliveryMailSender(
                session,
                properties.getHost(),
                properties.getUsername(),
                properties.getPassword()
        );
    }

    @Bean
    @Qualifier("ociJavaMailSender")
    JavaMailSender emailDeliveryJavaMailSender(Session session) {
        return new EmailDeliveryJavaMailSender(
                session,
                properties.getHost(),
                properties.getUsername(),
                properties.getPassword()
        );
    }

    @Bean
    Session session() {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", String.valueOf(properties.getPort()));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.auth.login.disable", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        return Session.getDefaultInstance(props);
    }
}
