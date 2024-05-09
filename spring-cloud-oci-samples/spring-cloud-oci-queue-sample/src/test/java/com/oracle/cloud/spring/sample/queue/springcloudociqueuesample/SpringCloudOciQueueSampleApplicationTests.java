/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.queue.springcloudociqueuesample;

import com.oracle.bmc.queue.requests.DeleteMessageRequest;
import com.oracle.bmc.queue.requests.DeleteQueueRequest;
import com.oracle.bmc.queue.responses.*;
import com.oracle.cloud.spring.queue.Queue;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Environment variables needed to run these tests are :
 * all variables in application-test.properties file,
 * queueName,
 * compartmentId
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfSystemProperty(named = "it.queue", matches = "true")
@TestPropertySource(locations = "classpath:application-test.properties")
class SpringCloudOciQueueSampleApplicationTests extends SpringCloudSampleApplicationTestBase {
    @Autowired
    Queue queue;

    @Value("${compartmentId}")
    String compartmentId;

    private static final long currentTimeMillis = System.currentTimeMillis();
    private static final String queueName = "sampleQueue-" + currentTimeMillis;
    private static String queueId;
    private static String messageReceipt;

    @Test
    @Order(1)
    void testCreateQueue() {
        long time = System.currentTimeMillis();
        queueId = queue.createQueue(queueName + time, compartmentId, 30, 30);
        assertNotNull(queueId);
    }

    @Test
    @Order(2)
    void testGetQueue() {
        GetQueueResponse response = queue.getQueue(queueId);
        assertNotNull(response);
        assertNotNull(response.getQueue());
    }

    @Test
    @Order(3)
    void testListQueues() throws Exception {
        ListQueuesResponse response = queue.listQueues(queueName, compartmentId);
        assertNotNull(response);
        int size = response.getQueueCollection().getItems().size();
        assertNotNull(size);
    }

    @Test
    @Order(4)
    void testPutMessages() {
        PutMessagesResponse response = queue.putMessages(queueId, new String[]{"Test Message1","Test Message2"});
        assertNotNull(response);
        int size = response.getPutMessages().getMessages().size();
        assertEquals(size, 2);
    }

    @Test
    @Order(5)
    void testGetMessages() {
        GetMessagesResponse response = queue.getMessages(queueId, 30, 30, 7);
        assertNotNull(response);
        int size = response.getGetMessages().getMessages().size();
        assertEquals(size, 2);
        messageReceipt = response.getGetMessages().getMessages().getFirst().getReceipt();
    }

    @Test
    @Order(6)
    void testUpdateMessages() {
        UpdateMessagesResponse response = queue.updateMessages(queueId, new String[]{messageReceipt}, 30);
        assertNotNull(response);
        int size = response.getUpdateMessagesResult().getEntries().size();
        assertEquals(size, 1);
    }

    @Test
    @Order(7)
    void testDeleteMessage() {
        DeleteMessageRequest request = DeleteMessageRequest.builder()
                .queueId(queueId)
                .messageReceipt(messageReceipt).build();
        DeleteMessageResponse response = queue.getQueueClient().deleteMessage(request);
        assertNotNull(response.getOpcRequestId());
    }

    @Test
    @Order(8)
    void testDeleteQueue() {
        DeleteQueueRequest request = DeleteQueueRequest.builder().queueId(queueId).build();
        DeleteQueueResponse response = queue.getQueueAdminClient().deleteQueue(request);
        assertNotNull(response.getOpcRequestId());
    }
}
