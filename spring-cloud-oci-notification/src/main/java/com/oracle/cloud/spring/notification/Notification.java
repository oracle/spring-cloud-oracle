/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.notification;

import com.oracle.bmc.ons.NotificationControlPlane;
import com.oracle.bmc.ons.NotificationDataPlane;
import com.oracle.bmc.ons.responses.*;

/**
 * Interface for defining OCI logging module.
 */
public interface Notification {

    /**
     * Direct instance of OCI Java SDK NotificationDataPlane Client.
     * @return NotificationDataPlane
     */
    NotificationDataPlane getNotificationDataPlaneClient();

    /**
     * Direct instance of OCI Java SDK NotificationControlPlane Client.
     * @return NotificationControlPlane
     */
    NotificationControlPlane getNotificationControlPlaneClient();

    /**
     * Publish a message to a Topic.
     * @param topicId OCID of the topic
     * @param title Message title
     * @param message Message content
     * @return PublishMessageResponse
     */
    PublishMessageResponse publishMessage(String topicId, String title, String message);

    /**
     * Create a Notification subscription in a Topic.
     * @param compartmentId Compartment OCID where the Subscription needs to be created
     * @param topicId Topic OCID where the Subscription needs to be created
     * @param protocol Subscription type. Ex: EMAIL
     * @param endpoint Subscription endpoint. Ex: Email ID in case of EMAIL as protocol
     * @return CreateSubscriptionResponse
     */
    CreateSubscriptionResponse createSubscription(String compartmentId, String topicId, String protocol, String endpoint);

    /**
     * Get the Subscription Resource JSON as a String.
     * @param subscriptionId OCID of the subscription
     * @return String
     */
    String getSubscription(String subscriptionId);

    /**
     * List subscriptions in a Topic as a JSON String.
     * @param topicId Topic OCID where to list the Subscriptions
     * @param compartmentId Compartment OCID where the topic is present
     * @return String
     */
    String listSubscriptions(String topicId, String compartmentId);

    /**
     * Create an OCI Notification Topic.
     * @param topicName Name of the Topic to be created
     * @param compartmentId Compartment OCID where the Topic needs to be created
     * @return CreateTopicResponse
     */
    CreateTopicResponse createTopic(String topicName, String compartmentId);
}
