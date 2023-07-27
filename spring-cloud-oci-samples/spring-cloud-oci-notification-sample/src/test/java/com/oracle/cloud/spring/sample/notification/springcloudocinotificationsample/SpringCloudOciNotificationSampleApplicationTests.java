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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Environment variables needed to run this tests are :
 * all variables in application-test.properties files,
 * topicName,
 * compartmentId
 */
@SpringBootTest
@EnabledIfSystemProperty(named = "it.notification", matches = "true")
@TestPropertySource(locations="classpath:application-test.properties")
class SpringCloudOciNotificationSampleApplicationTests extends SpringCloudSampleApplicationTestBase {
	@Autowired
	Notification notification;

	@Value("${topicName}")
	String topicName;

	@Value("${compartmentId}")
	String compartmentId;

	@Test
	void testNotificationApis() throws Exception {
		String topicOcid = testCreateTopic();
		String subscriptionOcid = testCreatSubscription(topicOcid);
		testGetSubscription(subscriptionOcid);
		testListSubscriptions(topicOcid);
		testPublishMessage(topicOcid);
		testDeleteTopic(topicOcid);
	}

	private String testCreateTopic() {
		long time = System.currentTimeMillis();
		CreateTopicResponse response = notification.createTopic(topicName + time, compartmentId);
		String topicOcid = response.getNotificationTopic().getTopicId();
		Assert.notNull(topicOcid);
		return topicOcid;
	}

	private String testCreatSubscription(String topicOcid) {
		CreateSubscriptionResponse response = notification.createSubscription(compartmentId, topicOcid, "EMAIL",
				"springcloud@oracle.com");
		String subscriptionOcid = response.getSubscription().getId();
		Assert.notNull(subscriptionOcid);
		return subscriptionOcid;
	}

	private Subscription testGetSubscription(String subscriptionOcid) throws Exception {
		String response = notification.getSubscription(subscriptionOcid);
		ObjectMapper objectMapper = new ObjectMapper();
		Subscription subscription = objectMapper.readValue(response, new TypeReference<Subscription>(){});
		Assert.notNull(subscription);
		return subscription;
	}

	private void testListSubscriptions(String topicOcid) throws Exception {
		String listSubscriptions = notification.listSubscriptions(topicOcid,
				compartmentId);
		ObjectMapper objectMapper = new ObjectMapper();
		List<SubscriptionSummary> notificationList =
				objectMapper.readValue(listSubscriptions, new TypeReference<List<SubscriptionSummary>>(){});
		Assert.isTrue(notificationList.size() > 0);
	}

	private void testPublishMessage(String topicOcid) {
		PublishMessageResponse response = notification.publishMessage(topicOcid,
				"integration-test-subject", "integration-test-message");
		Assert.notNull(response.getPublishResult().getMessageId());
	}

	private void testDeleteTopic(String topicOcid) {
		DeleteTopicRequest request = DeleteTopicRequest.builder().topicId(topicOcid).build();
		NotificationControlPlane controlPlane = notification.getNotificationControlPlaneClient();
		DeleteTopicResponse response = controlPlane.deleteTopic(request);
		Assert.notNull(response.getOpcRequestId());
	}
}
