/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.notification.springcloudocinotificationsample;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.bmc.ons.NotificationControlPlane;
import com.oracle.bmc.ons.model.Subscription;
import com.oracle.bmc.ons.model.SubscriptionSummary;
import com.oracle.bmc.ons.requests.DeleteTopicRequest;
import com.oracle.bmc.ons.responses.CreateSubscriptionResponse;
import com.oracle.bmc.ons.responses.CreateTopicResponse;
import com.oracle.bmc.ons.responses.DeleteTopicResponse;
import com.oracle.bmc.ons.responses.PublishMessageResponse;

import com.oracle.cloud.spring.notification.Notification;
import com.oracle.cloud.spring.sample.common.base.SpringCloudSampleApplicationTestBase;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Environment variables needed to run this tests are :
 * all variables in application-test.properties files,
 * topicName,
 * compartmentId
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfSystemProperty(named = "it.notification", matches = "true")
@TestPropertySource(locations="classpath:application-test.properties")
class SpringCloudOciNotificationSampleApplicationTests extends SpringCloudSampleApplicationTestBase {
	@Autowired
	Notification notification;

	@Value("${topicName}")
	String topicName;

	@Value("${compartmentId}")
	String compartmentId;

	private static String topicOcid;
	private static String subscriptionOcid;

	@Test
	@Order(1)
	void testCreateTopic() {
		long time = System.currentTimeMillis();
		CreateTopicResponse response = notification.createTopic(topicName + time, compartmentId);
		topicOcid = response.getNotificationTopic().getTopicId();
		assertNotNull(topicOcid);
	}

	@Test
	@Order(2)
	void testCreatSubscription() {
		CreateSubscriptionResponse response = notification.createSubscription(compartmentId, topicOcid, "EMAIL",
				"springcloud@oracle.com");
		subscriptionOcid = response.getSubscription().getId();
		assertNotNull(subscriptionOcid);
	}

	@Test
	@Order(3)
	void testGetSubscription() throws Exception {
		String response = notification.getSubscription(subscriptionOcid);
		ObjectMapper objectMapper = new ObjectMapper();
		Subscription subscription = objectMapper.readValue(response, new TypeReference<Subscription>(){});
		assertNotNull(subscription);
	}

	@Test
	@Order(4)
	void testListSubscriptions() throws Exception {
		String listSubscriptions = notification.listSubscriptions(topicOcid,
				compartmentId);
		ObjectMapper objectMapper = new ObjectMapper();
		List<SubscriptionSummary> notificationList =
				objectMapper.readValue(listSubscriptions, new TypeReference<List<SubscriptionSummary>>(){});
		assertEquals(notificationList.size(), 1);
	}

	@Test
	@Order(5)
	void testPublishMessage() {
		PublishMessageResponse response = notification.publishMessage(topicOcid,
				"integration-test-subject", "integration-test-message");
		assertNotNull(response.getPublishResult().getMessageId());
	}

	@Test
	@Order(6)
	void testDeleteTopic() {
		DeleteTopicRequest request = DeleteTopicRequest.builder().topicId(topicOcid).build();
		NotificationControlPlane controlPlane = notification.getNotificationControlPlaneClient();
		DeleteTopicResponse response = controlPlane.deleteTopic(request);
		assertNotNull(response.getOpcRequestId());
	}
}
