// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.sample.email.springcloudemailsample;

import java.io.File;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final MailSender mailSender;

    public EmailService(@Qualifier("ociJavaMailSender") JavaMailSender javaMailSender, @Qualifier("ociMailSender") MailSender mailSender) {
        this.javaMailSender = javaMailSender;
        this.mailSender = mailSender;
    }

    public void sendJavaMail(
            String from,
            String to,
            String subject,
            String text,
            File fileAttachment
    ) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        Multipart multipartContent = new MimeMultipart();
        MimeBodyPart textContent = new MimeBodyPart();
        textContent.setContent(text, "text/html");
        multipartContent.addBodyPart(textContent);

        MimeBodyPart attachmentContent = new MimeBodyPart();
        DataSource source = new FileDataSource(fileAttachment);
        attachmentContent.setDataHandler(new DataHandler(source));
        attachmentContent.setFileName(fileAttachment.getName());
        multipartContent.addBodyPart(attachmentContent);

        message.setFrom(from);
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(multipartContent);
        javaMailSender.send(message);
    }

    public void sendSimpleMail(
            String from,
            String to,
            String subject,
            String text
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
