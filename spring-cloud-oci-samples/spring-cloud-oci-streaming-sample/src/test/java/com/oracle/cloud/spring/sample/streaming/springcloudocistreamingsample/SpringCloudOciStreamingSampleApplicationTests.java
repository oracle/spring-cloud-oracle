/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.streaming.springcloudocistreamingsample;

import com.oracle.bmc.streaming.model.*;
import com.oracle.bmc.streaming.requests.GetStreamPoolRequest;
import com.oracle.bmc.streaming.requests.GetStreamRequest;
import com.oracle.bmc.streaming.responses.*;
import com.oracle.bmc.waiter.Waiter;
import com.oracle.cloud.spring.sample.common.base.SpringCloudSampleApplicationTestBase;
import com.oracle.cloud.spring.streaming.Streaming;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Environment variables needed to run this tests are :
 * all variables in application-test.properties files,
 * compartmentId
 */

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfSystemProperty(named = "it.streaming", matches = "true")
@TestPropertySource(locations="classpath:application-test.properties")
class SpringCloudOciStreamingSampleApplicationTests extends SpringCloudSampleApplicationTestBase {
	@Autowired
	Streaming streaming;

	@Value("${compartmentId}")
	String compartmentId;

	private static final long currentTimeMillis = System.currentTimeMillis();
	private static final String key = "sampleKey-" + currentTimeMillis;

	private static final String value = "sampleValue-" + currentTimeMillis;

	private static String cursor;

	private static String streamPoolId;
	private static String streamId;

	@Test
	@Order(1)
	void testCreateStreamPool() throws Exception {
		CreateStreamPoolResponse response = streaming.createStreamPool("SCStreamPool-" + currentTimeMillis, compartmentId);
		streamPoolId = response.getStreamPool().getId();
		Waiter<GetStreamPoolRequest, GetStreamPoolResponse> waiter = streaming.getAdminClient().getWaiters().
				forStreamPool(GetStreamPoolRequest.builder().streamPoolId(streamPoolId).build(),
						StreamPool.LifecycleState.Active);
		waiter.execute();
		assertNotNull(streamPoolId);
	}

	@Test
	@Order(2)
	void testCreateStream()  throws Exception {
		CreateStreamResponse response = streaming.createStream("SCStream-" + currentTimeMillis, streamPoolId,
				1, null);
		streamId = response.getStream().getId();
		Waiter<GetStreamRequest, GetStreamResponse> waiter = streaming.getAdminClient().getWaiters().
				forStream(GetStreamRequest.builder().streamId(streamId).build(),
						Stream.LifecycleState.Active);
		waiter.execute();
		assertNotNull(streamId);
	}

	@Test
	@Order(3)
	void testCreateGroupCursor() {
		CreateGroupCursorResponse response = streaming.createGroupCursor(streamId, "testgroup", null,
				CreateGroupCursorDetails.Type.Latest, true, "p0", null);
		assertNotNull(response.getCursor());
		String groupCursor = response.getCursor().getValue();
		System.out.println("Group cursor : " + groupCursor);
		assertNotNull(groupCursor);
	}

	@Test
	@Order(4)
	void testCreateCursor() {
		CreateCursorResponse response = streaming.createCursor(streamId, null, null,
				CreateCursorDetails.Type.Latest, "0");
		assertNotNull(response.getCursor());
		cursor = response.getCursor().getValue();
		System.out.println("Cursor : " + cursor);
		assertNotNull(cursor);
	}

	@Test
	@Order(5)
	void testPutMessages() {
		PutMessagesResponse response = streaming.putMessages(streamId, key.getBytes(), Arrays.asList(value.getBytes()));
		assertNotNull(response);
		int size = response.getPutMessagesResult().getEntries().size();
		assertEquals(size, 1);
	}

	@Test
	@Order(6)
	void testGetMessages() {
		GetMessagesResponse response = streaming.getMessages(streamId, cursor);
		assertNotNull(response);
		int messagesSize =  response.getItems().size();
		System.out.println("total message size : " + messagesSize);
		assertTrue(messagesSize > 0);
		String message = new String(response.getItems().get(messagesSize-1).getValue());
		System.out.println("message : " + message);
		System.out.println("value : " + value);
		assertNotNull(message);
		assertEquals(value, message);
	}

	@Test
	@Order(7)
	void testPutMessagesInBulk() {
		List<PutMessagesDetailsEntry> messages = new ArrayList<>();
		PutMessagesDetailsEntry entry1 = PutMessagesDetailsEntry.builder().
				key((key + 1).getBytes()).value((value + 1).getBytes()).build();
		PutMessagesDetailsEntry entry2 = PutMessagesDetailsEntry.builder().
				key((key + 2).getBytes()).value((value + 2).getBytes()).build();
		PutMessagesDetailsEntry entry3 = PutMessagesDetailsEntry.builder().value((value + 3).getBytes()).build();
		messages.add(entry1);
		messages.add(entry2);
		messages.add(entry3);
		PutMessagesDetails putMessagesDetails = PutMessagesDetails.builder().messages(messages).build();
		PutMessagesResponse response = streaming.putMessages(streamId, putMessagesDetails);
		assertNotNull(response);
		int size = response.getPutMessagesResult().getEntries().size();
		assertEquals(size, 3);
	}

	@Test
	@Order(8)
	void deleteStream() throws Exception {
		DeleteStreamResponse response = streaming.deleteStream(streamId);
		assertNotNull(response.getOpcRequestId());
		Waiter<GetStreamRequest, GetStreamResponse> waiter = streaming.getAdminClient().getWaiters().
				forStream(GetStreamRequest.builder().streamId(streamId).build(),
						Stream.LifecycleState.Deleted);
		waiter.execute();
	}

	@Test
	@Order(9)
	void deleteStreamPool() throws Exception {
		DeleteStreamPoolResponse response = streaming.deleteStreamPool(streamPoolId);
		assertNotNull(response.getOpcRequestId());
		Waiter<GetStreamPoolRequest, GetStreamPoolResponse> waiter = streaming.getAdminClient().getWaiters().
				forStreamPool(GetStreamPoolRequest.builder().streamPoolId(streamPoolId).build(),
						StreamPool.LifecycleState.Deleted);
		waiter.execute();
	}
}
