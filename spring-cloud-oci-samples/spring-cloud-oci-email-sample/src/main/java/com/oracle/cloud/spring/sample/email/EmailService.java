/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(MailContent content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(content.from);
        message.setTo(content.to);
        message.setSubject(content.subject);
        message.setText(content.text);
        emailSender.send(message);
    }

    public static class MailContent {
        @JsonProperty("from")
        String from;

        @JsonProperty("fromPersonal")
        String fromPersonal;

        @JsonProperty("to")
        String to;

        @JsonProperty("subject")
        String subject;

        @JsonProperty("text")
        String text;

        public MailContent() {}
    }
}
