/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
package com.oracle.cloud.spring.email;

import java.util.Objects;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class EmailDeliveryMailSender implements MailSender {
    protected final Session session;
    private final String smtpHost;
    private final String smtpUsername;
    private final String smtpPassword;

    public EmailDeliveryMailSender(Session session, String smtpHost, String smtpUsername, String smtpPassword) {
        this.session = session;
        this.smtpHost = smtpHost;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    @Override
    public void send(SimpleMailMessage simpleMailMessage) throws MailException {
        MimeMessage message;
        try {
            message = toMessage(simpleMailMessage);
        } catch (MessagingException e) {
            throw new MailPreparationException(e);
        }
        try {
            sendMimeMessage(message);
        } catch (MailException e) {
            throw new MailSendException(e.getMessage(), e);
        }
    }

    @Override
    public void send(SimpleMailMessage... messages) throws MailException {
        for (SimpleMailMessage message : messages) {
            send(message);
        }
    }

    protected void sendMimeMessage(MimeMessage message) throws MailException {
        try (Transport transport = session.getTransport()) {
            transport.connect(smtpHost, smtpUsername, smtpPassword);

            transport.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException e) {
            throw new MailPreparationException(e);
        }
    }

    private MimeMessage toMessage(SimpleMailMessage simpleMailMessage) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        Address from = new InternetAddress(Objects.requireNonNull(simpleMailMessage.getFrom(), "From address is null"));
        message.setFrom(from);
        message.setSubject(simpleMailMessage.getSubject());
        if (simpleMailMessage.getTo() != null) {
            message.setRecipients(Message.RecipientType.TO, toAddresses(simpleMailMessage.getTo()));
        }
        if (simpleMailMessage.getCc() != null) {
            message.setRecipients(Message.RecipientType.CC, toAddresses(simpleMailMessage.getCc()));
        }
        if (simpleMailMessage.getBcc() != null) {
            message.setRecipients(Message.RecipientType.BCC, toAddresses(simpleMailMessage.getBcc()));
        }
        if (simpleMailMessage.getReplyTo() != null) {
            message.setReplyTo(toAddresses(new String[]{simpleMailMessage.getReplyTo()}));
        }
        if (simpleMailMessage.getSentDate() != null) {
            message.setSentDate(simpleMailMessage.getSentDate());
        }
        if (simpleMailMessage.getText() != null) {
            message.setText(simpleMailMessage.getText());
        }
        return message;
    }

    private Address[] toAddresses(String[] recipients) throws AddressException {
        Address[] addresses = new Address[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addresses[i] = new InternetAddress(recipients[i]);
        }
        return addresses;
    }

}
