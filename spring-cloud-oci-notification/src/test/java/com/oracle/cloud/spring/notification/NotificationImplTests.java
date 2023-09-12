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

class NotificationImplTests {

    final Notification notification = mock(NotificationImpl.class);

    @Test
    void testNotificationControlPlaneClient() {
        when(notification.getNotificationControlPlaneClient()).thenReturn(mock(NotificationControlPlane.class));

        assertDoesNotThrow(() -> { notification.createTopic("topic", "compartment");});
        assertNull(notification.getNotificationDataPlaneClient());

    }

    @Test
    void testNotificationDataPlaneClient() {
        when(notification.getNotificationDataPlaneClient()).thenReturn(mock(NotificationDataPlane.class));

        assertDoesNotThrow(() -> { notification.publishMessage("topic", "title", "message");});
        assertNull(notification.getNotificationControlPlaneClient());
    }
}
