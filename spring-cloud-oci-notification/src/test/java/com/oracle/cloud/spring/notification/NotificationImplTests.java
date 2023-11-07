/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
*/

package com.oracle.cloud.spring.notification;

import com.oracle.bmc.ons.NotificationControlPlane;
import com.oracle.bmc.ons.NotificationDataPlane;
import com.oracle.bmc.ons.model.NotificationTopic;
import com.oracle.bmc.ons.model.PublishResult;
import com.oracle.bmc.ons.model.Subscription;
import com.oracle.bmc.ons.responses.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class NotificationImplTests {

    final NotificationDataPlane notificationDataPlane = mock(NotificationDataPlane.class);
    final NotificationControlPlane notificationControlPlane = mock(NotificationControlPlane.class);
    final Notification notification = new NotificationImpl(notificationDataPlane, notificationControlPlane);

    @Test
    void testNotificationControlPlaneClient() {

        CreateTopicResponse createTopicResponse = mock(CreateTopicResponse.class);
        when(createTopicResponse.getNotificationTopic()).thenReturn(mock(NotificationTopic.class));
        when(notificationControlPlane.createTopic(any())).thenReturn(createTopicResponse);
        assertNotNull(notification.createTopic("topic", "compartment"));
        assertNotNull(notification.getNotificationControlPlaneClient());
    }

    @Test
    void testNotificationDataPlaneClient() {

        PublishMessageResponse publishMessageResponse = mock(PublishMessageResponse.class);
        when(publishMessageResponse.getPublishResult()).thenReturn(mock(PublishResult.class));
        when(notificationDataPlane.publishMessage(any())).thenReturn(publishMessageResponse);
        assertNotNull(notification.publishMessage("topic", "title", "message"));

        CreateSubscriptionResponse createSubscriptionResponse = mock(CreateSubscriptionResponse.class);
        when(createSubscriptionResponse.getSubscription()).thenReturn(mock(Subscription.class));
        when(notificationDataPlane.createSubscription(any())).thenReturn(createSubscriptionResponse);
        assertNotNull(notification.createSubscription("compartmentId", "title", "protocol", "endpoint"));

        GetSubscriptionResponse getSubscriptionResponse = mock(GetSubscriptionResponse.class);
        when(getSubscriptionResponse.getSubscription()).thenReturn(mock(Subscription.class));
        when(notificationDataPlane.getSubscription(any())).thenReturn(getSubscriptionResponse);
        assertNotNull(notification.getSubscription("subscriptionId"));

        when(notificationDataPlane.listSubscriptions(any())).thenReturn(mock(ListSubscriptionsResponse.class));
        assertNotNull(notification.listSubscriptions("topic", "compartmentId"));
        assertNotNull(notification.getNotificationDataPlaneClient());
    }
}
