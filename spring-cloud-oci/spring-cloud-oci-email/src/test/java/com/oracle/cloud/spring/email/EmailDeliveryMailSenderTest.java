package com.oracle.cloud.spring.email;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EmailDeliveryMailSenderTest extends EmailTest {
    @Test
    void sendNoFrom() throws MessagingException {
        SimpleMailMessage message = getMessage();
        message.setFrom(null);
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> getSender().send(message))
                .withMessage("From address is null");
        verify(transport, times(0)).sendMessage(any(), any());
    }

    @Test
    void sendMessage() throws MessagingException {
        getSender().send(getMessage());
        verify(transport, times(1)).sendMessage(any(), any());
    }

    @Test
    void sendMessages() throws MessagingException {
        getSender().send(getMessage(), getMessage(), getMessage());
        verify(transport, times(3)).sendMessage(any(), any());
    }

    private EmailDeliveryMailSender getSender() {
        return new EmailDeliveryMailSender(session, "x", "y", "z");
    }

    private SimpleMailMessage getMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(testEmail);
        message.setTo(testEmail);
        message.setCc(testEmail2);
        message.setBcc(testEmail3);
        message.setSubject(testSubject);
        message.setText(testBody);
        message.setReplyTo(testEmail2);
        return message;
    }
}
