/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.notification;

import com.oracle.bmc.ons.NotificationControlPlane;
import com.oracle.bmc.ons.NotificationDataPlane;
import com.oracle.bmc.ons.model.CreateSubscriptionDetails;
import com.oracle.bmc.ons.model.CreateTopicDetails;
import com.oracle.bmc.ons.model.MessageDetails;
import com.oracle.bmc.ons.model.PublishResult;
import com.oracle.bmc.ons.requests.*;
import com.oracle.bmc.ons.responses.*;
import com.oracle.cloud.spring.core.util.OCIObjectMapper;

/**
 * Implementation for the OCI Notification module.
 */
public class NotificationImpl implements Notification {
    private final NotificationDataPlane notificationDataPlane;
    private final NotificationControlPlane notificationControlPlane;

    public NotificationImpl(NotificationDataPlane notificationDataPlane, NotificationControlPlane notificationControlPlane) {
        this.notificationDataPlane = notificationDataPlane;
        this.notificationControlPlane = notificationControlPlane;
    }

    /**
     * Direct instance of OCI Java SDK NotificationDataPlane Client.
     * @return NotificationDataPlane
     */
    public NotificationDataPlane getNotificationDataPlaneClient() {
        return notificationDataPlane;
    }

    /**
     * Direct instance of OCI Java SDK NotificationControlPlane Client.
     * @return NotificationControlPlane
     */
    public NotificationControlPlane getNotificationControlPlaneClient() {
        return notificationControlPlane;
    }

    /**
     * Publish message to a Topic.
     * @param topicId OCID of the topic
     * @param title Message title
     * @param message Message content
     * @return PublishMessageResponse
     */
    @Override
    public PublishMessageResponse publishMessage(String topicId, String title, String message) {

        MessageDetails.Builder messageDetailsBuilder = MessageDetails.builder();
        messageDetailsBuilder.title(title).body(message);
        MessageDetails messageDetails = messageDetailsBuilder.build();

        PublishMessageRequest.Builder builder = PublishMessageRequest.builder();
        builder.topicId(topicId).messageDetails(messageDetails);

        PublishMessageRequest request = builder.build();
        PublishMessageResponse response = notificationDataPlane.publishMessage(request);
        PublishResult result = response.getPublishResult();
        System.out.println(" result messageId : " + result.getMessageId());
        return response;
    }

    /**
     * Creates a Notification subscription in a Topic.
     * @param compartmentId Compartment OCID where the Subscription needs to be created
     * @param topicId Topic OCID where the Subscription needs to be created
     * @param protocol Subscription type. Ex: EMAIL
     * @param endpoint Subscription endpoint. Ex: Email ID in case of EMAIL as protocol
     * @return CreateSubscriptionResponse
     */
    @Override
    public CreateSubscriptionResponse createSubscription(String compartmentId, String topicId, String protocol, String endpoint) {
        CreateSubscriptionDetails details = CreateSubscriptionDetails.builder().compartmentId(compartmentId).topicId(topicId).protocol(protocol).endpoint(endpoint).build();
        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder().createSubscriptionDetails(details).build();
        CreateSubscriptionResponse response = notificationDataPlane.createSubscription(request);
        System.out.println(" result subscriptionId : " + response.getSubscription().getId());
        return response;
    }

    /**
     * Get the Subscription Resource JSON as a String.
     * @param subscriptionId OCID of the subscription
     * @return String
     */
    @Override
    public String getSubscription(String subscriptionId) {
        GetSubscriptionRequest request = GetSubscriptionRequest.builder().subscriptionId(subscriptionId).build();
        GetSubscriptionResponse response = notificationDataPlane.getSubscription(request);
        System.out.println(" result subscription : " + response.getSubscription());
        String jsonObjectString = OCIObjectMapper.toPrintableString(response.getSubscription());
        return jsonObjectString;
    }

    /**
     * List subscriptions in a Topic as a JSON String.
     * @param topicId Topic OCID where to list the Subscriptions
     * @param compartmentId Compartment OCID where the topic is present
     * @return String
     */
    @Override
    public String listSubscriptions(String topicId, String compartmentId) {
        ListSubscriptionsRequest request = ListSubscriptionsRequest.builder().topicId(topicId).
                compartmentId(compartmentId).build();
        ListSubscriptionsResponse response = notificationDataPlane.listSubscriptions(request);
        System.out.println(" result subscriptions : " + response.getItems());
        String jsonArrayString = OCIObjectMapper.toPrintableString(response.getItems());
        return jsonArrayString;
    }

    /**
     * Creates an OCI Notification Topic.
     * @param topicName Name of the Topic to be created
     * @param compartmentId Compartment OCID where the Topic needs to be created
     * @return CreateTopicResponse
     */
    @Override
    public CreateTopicResponse createTopic(String topicName, String compartmentId) {
        CreateTopicDetails details = CreateTopicDetails.builder().name(topicName).compartmentId(compartmentId).build();
        CreateTopicRequest request = CreateTopicRequest.builder().createTopicDetails(details).build();
        CreateTopicResponse response = notificationControlPlane.createTopic(request);
        System.out.println(" result topicId : " + response.getNotificationTopic().getTopicId());
        return response;
    }


}
