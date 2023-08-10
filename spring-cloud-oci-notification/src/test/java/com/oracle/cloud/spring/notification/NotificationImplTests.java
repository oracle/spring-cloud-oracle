/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
*/

package com.oracle.cloud.spring.notification;

import com.oracle.bmc.ons.NotificationControlPlane;
import com.oracle.bmc.ons.NotificationDataPlane;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class NotificationImplTests {

    final Notification notification = mock(NotificationImpl.class);

    @Test
    public void testNotificationControlPlaneClient() {
        when(notification.getNotificationControlPlaneClient()).thenReturn(mock(NotificationControlPlane.class));

        assertDoesNotThrow(() -> { notification.createTopic("topic", "compartment");});
        assertNotNull(notification.getNotificationControlPlaneClient());
        assertNull(notification.getNotificationDataPlaneClient());

    }

    @Test
    public void testNotificationDataPlaneClient() {
        when(notification.getNotificationDataPlaneClient()).thenReturn(mock(NotificationDataPlane.class));

        assertDoesNotThrow(() -> { notification.publishMessage("topic", "title", "message");});
        assertNotNull(notification.getNotificationDataPlaneClient());
        assertNull(notification.getNotificationControlPlaneClient());
    }
}
