package com.oracle.cloud.spring.email;

import java.util.Properties;

import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmailTest {
    final String testEmail = "test@test.xyz";
    final String testEmail2 = "test2@test.xyz";
    final String testEmail3 = "test3@test.xyz";
    final String testSubject = "test subject";
    final String testBody = "test body";
    Session session;
    Transport transport;

    @BeforeEach
    void setUpEach() throws NoSuchProviderException {
        session = mock(Session.class);
        transport = mock(Transport.class);
        when(session.getTransport()).thenReturn(transport);
        when(session.getProperties()).thenReturn(new Properties());
    }
}
