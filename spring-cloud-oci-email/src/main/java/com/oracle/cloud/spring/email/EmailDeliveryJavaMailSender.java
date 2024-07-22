/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
package com.oracle.cloud.spring.email;

import java.io.InputStream;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailDeliveryJavaMailSender extends EmailDeliveryMailSender implements JavaMailSender {
    public EmailDeliveryJavaMailSender(Session session, String smtpHost, String smtpUsername, String smtpPassword) {
        super(session, smtpHost, smtpUsername, smtpPassword);
    }

    @Override
    public MimeMessage createMimeMessage() {
        return new MimeMessage(session);
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        try {
            return new MimeMessage(session, contentStream);
        } catch (MessagingException e) {
            throw new MailParseException(e);
        }
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        for (MimeMessage mimeMessage : mimeMessages) {
            sendMimeMessage(mimeMessage);
        }
    }
}
