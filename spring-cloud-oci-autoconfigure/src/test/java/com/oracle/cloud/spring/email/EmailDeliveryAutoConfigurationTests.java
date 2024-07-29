// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.email;

import com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans;
import com.oracle.cloud.spring.vault.VaultAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailDeliveryAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EmailDeliveryAutoConfiguration.class))
            .withUserConfiguration(TestCommonConfigurationBeans.class);

    @Test
    void beansAreLoaded() {
        contextRunner.run(context -> {
            JavaMailSender javaMailSender = BeanFactoryAnnotationUtils.qualifiedBeanOfType(context.getBeanFactory(), JavaMailSender.class, "ociJavaMailSender");
            assertThat(javaMailSender instanceof EmailDeliveryJavaMailSender).isTrue();
            MailSender mailSender = BeanFactoryAnnotationUtils.qualifiedBeanOfType(context.getBeanFactory(), MailSender.class, "ociMailSender");
            assertThat(mailSender instanceof EmailDeliveryMailSender).isTrue();
        });
    }
}
