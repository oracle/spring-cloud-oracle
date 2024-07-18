package com.oracle.cloud.spring.email;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EmailDeliveryJavaMailSenderTest extends EmailTest {
    @Test
    void sendMessage() throws MessagingException {
        EmailDeliveryJavaMailSender sender = getSender();
        sender.send(getMimeMessage(sender));
        verify(transport, times(1)).sendMessage(any(), any());
    }

    @Test
    void sendMessages() throws MessagingException {
        EmailDeliveryJavaMailSender sender = getSender();
        sender.send(getMimeMessage(sender), getMimeMessage(sender), getMimeMessage(sender));
        verify(transport, times(3)).sendMessage(any(), any());
    }

    private EmailDeliveryJavaMailSender getSender() {
        return new EmailDeliveryJavaMailSender(session, "x", "y", "z");
    }

    private MimeMessage getMimeMessage(EmailDeliveryJavaMailSender sender) throws MessagingException {
        MimeMessage message = sender.createMimeMessage();
        message.setFrom(testEmail);
        message.setRecipients(Message.RecipientType.TO, testEmail2);
        message.setText(testBody);
        message.setSubject(testSubject);
        return message;
    }
}
