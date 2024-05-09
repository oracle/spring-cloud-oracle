/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.queue;

import com.oracle.bmc.queue.QueueAdminClient;
import com.oracle.bmc.queue.QueueAdminWaiters;
import com.oracle.bmc.queue.QueueClient;
import com.oracle.bmc.queue.model.QueueCollection;
import com.oracle.bmc.queue.model.WorkRequest;
import com.oracle.bmc.queue.model.WorkRequestResource;
import com.oracle.bmc.queue.responses.*;
import com.oracle.bmc.waiter.Waiter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class QueueImplTests {
    final QueueAdminClient queueAdminClient = mock(QueueAdminClient.class);

    final QueueClient queueClient = mock(QueueClient.class);

    final Queue queue = new QueueImpl(queueAdminClient, queueClient);

    @Test
    void testQueueAdminClient() {
        when(queueAdminClient.createQueue(any())).thenReturn(mock(CreateQueueResponse.class));
        when(queueAdminClient.getQueue(any())).thenReturn(mock(GetQueueResponse.class));
        when(queueAdminClient.listQueues(any())).thenReturn(mock(ListQueuesResponse.class));
        when(queueAdminClient.deleteQueue(any())).thenReturn(mock(DeleteQueueResponse.class));

        when(queueAdminClient.getWorkRequest(any())).thenReturn(mock(GetWorkRequestResponse.class));
        when(queueAdminClient.getWaiters()).thenReturn(mock(QueueAdminWaiters.class));
        when(queueAdminClient.getWaiters().forWorkRequest(any())).thenReturn(mock(Waiter.class));
        when(queueAdminClient.getWorkRequest(any()).getWorkRequest()).thenReturn(mock(WorkRequest.class));
        when(queueAdminClient.getWorkRequest(any()).getWorkRequest().getResources()).thenReturn(Arrays.asList(mock(WorkRequestResource.class)));
        when(queueAdminClient.getWorkRequest(any()).getWorkRequest().getResources().getFirst().getIdentifier()).thenReturn("queueId");
        assertNotNull(queue.getQueueAdminClient());
        String queueId = queue.createQueue("test", "compartmentId", 30, 30);
        assertNotNull(queueId);

        GetQueueResponse getQueueResponse = queue.getQueue(queueId);
        assertNotNull(getQueueResponse);

        when(queueAdminClient.listQueues(any()).getQueueCollection()).thenReturn(mock(QueueCollection.class));
        ListQueuesResponse listQueuesResponse = queue.listQueues("test", "compartmentId");
        assertNotNull(listQueuesResponse);
        DeleteQueueResponse deleteQueueResponse = queue.deleteQueue(queueId);
        assertNotNull(deleteQueueResponse);
    }

    @Test
    void testQueueClient() {
        when(queueClient.putMessages(any())).thenReturn(mock(PutMessagesResponse.class));
        when(queueClient.getMessages(any())).thenReturn(mock(GetMessagesResponse.class));
        when(queueClient.updateMessages(any())).thenReturn(mock(UpdateMessagesResponse.class));
        when(queueClient.deleteMessage(any())).thenReturn(mock(DeleteMessageResponse.class));
        assertNotNull(queue.getQueueClient());
        PutMessagesResponse putMessagesResponse = queue.putMessages("queueId", new String[]{"message1", "message2"});
        assertNotNull(putMessagesResponse);
        GetMessagesResponse getMessagesResponse = queue.getMessages("queueId", 30, 10, 7);
        assertNotNull(getMessagesResponse);
        UpdateMessagesResponse updateMessagesResponse = queue.updateMessages("queueId", new String[]{"receipt1", "receipt2"}, 30);
        assertNotNull(updateMessagesResponse);
        DeleteMessageResponse deleteMessageResponse = queue.deleteMessage("queueId", "receipt2");
        assertNotNull(deleteMessageResponse);
    }
}
