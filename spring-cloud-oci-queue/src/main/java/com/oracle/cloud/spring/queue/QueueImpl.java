/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.queue;

import com.oracle.bmc.queue.QueueAdminClient;
import com.oracle.bmc.queue.QueueClient;
import com.oracle.bmc.queue.model.*;
import com.oracle.bmc.queue.requests.*;
import com.oracle.bmc.queue.responses.*;
import com.oracle.bmc.waiter.Waiter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for the OCI Queue module.
 */
public class QueueImpl implements Queue {
    private final QueueAdminClient queueAdminClient;
    private final QueueClient queueClient;

    public QueueImpl(QueueAdminClient queueAdminClient, QueueClient queueClient) {
        this.queueAdminClient = queueAdminClient;
        this.queueClient = queueClient;
    }

    /**
     * Direct instance of OCI Java SDK QueueAdminClient.
     *
     * @return QueueAdminClient
     */
    public QueueAdminClient getQueueAdminClient() {
        return queueAdminClient;
    }

    /**
     * Direct instance of OCI Java SDK QueueClient.
     *
     * @return QueueClient
     */
    public QueueClient getQueueClient() {
        return queueClient;
    }

    /**
     * Put messages into a queue.
     *
     * @param queueId  OCID of the queue
     * @param messages list of messages
     * @return PutMessagesResponse
     */
    @Override
    public PutMessagesResponse putMessages(String queueId, String[] messages) {
        List<PutMessagesDetailsEntry> messagesDetailsEntries = new ArrayList<>();
        for (String message : messages) {
            messagesDetailsEntries.add(PutMessagesDetailsEntry.builder().content(message).build());
        }
        PutMessagesDetails messageDetails = PutMessagesDetails.builder()
                .messages(messagesDetailsEntries).build();
        PutMessagesRequest request = PutMessagesRequest.builder().queueId(queueId).putMessagesDetails(messageDetails).build();
        PutMessagesResponse response = queueClient.putMessages(request);
        return response;
    }

    /**
     * Get/consume messages from a queue.
     *
     * @param queueId             OCID of the queue
     * @param visibilityInSeconds Messages will be hidden and won't be consumable by other consumers for this time
     * @param timeoutInSeconds    Timeout for the request
     * @param limit               The maximum number of messages returned
     * @return GetMessagesResponse
     */
    @Override
    public GetMessagesResponse getMessages(String queueId, Integer visibilityInSeconds, Integer timeoutInSeconds, Integer limit) {
        GetMessagesRequest request = GetMessagesRequest.builder()
                .queueId(queueId)
                .visibilityInSeconds(visibilityInSeconds)
                .timeoutInSeconds(timeoutInSeconds)
                .limit(limit).build();
        GetMessagesResponse response = queueClient.getMessages(request);
        return response;
    }

    /**
     * Update the messages by their receipt in a queue.
     *
     * @param queueId             OCID of the queue
     * @param messageReceipts     List of message receipts to be updated
     * @param visibilityInSeconds Messages will be hidden and won't be consumable by other consumers for this time
     * @return UpdateMessagesResponse
     */
    @Override
    public UpdateMessagesResponse updateMessages(String queueId, String[] messageReceipts, Integer visibilityInSeconds) {
        List<UpdateMessagesDetailsEntry> messagesDetailsEntries = new ArrayList<>();
        for (String messageReceipt : messageReceipts) {
            messagesDetailsEntries.add(UpdateMessagesDetailsEntry.builder().receipt(messageReceipt)
                    .visibilityInSeconds(visibilityInSeconds).build());
        }
        UpdateMessagesDetails updateMessageDetails = UpdateMessagesDetails.builder()
                .entries(messagesDetailsEntries)
                .build();
        UpdateMessagesRequest request = UpdateMessagesRequest.builder()
                .queueId(queueId)
                .updateMessagesDetails(updateMessageDetails).build();
        UpdateMessagesResponse response = queueClient.updateMessages(request);
        return response;
    }

    /**
     * Delete the message by the receipt from a queue
     *
     * @param queueId        OCID of the queue
     * @param messageReceipt The receipt of the message to be deleted
     * @return DeleteMessageResponse
     */
    @Override
    public DeleteMessageResponse deleteMessage(String queueId, String messageReceipt) {
        DeleteMessageRequest request = DeleteMessageRequest.builder()
                .queueId(queueId)
                .messageReceipt(messageReceipt).build();
        DeleteMessageResponse response = queueClient.deleteMessage(request);
        return response;
    }

    /**
     * Creates a queue in a compartment.
     *
     * @param queueName                    Name of the queue to be created
     * @param compartmentId                Compartment OCID where the Queue needs to be created
     * @param deadLetterQueueDeliveryCount The number of times a message is delivered before its moved to the dead letter queue
     * @param retentionInSeconds           The time a message remains in the queue before its deleted by the service if it is not deleted by a consumer
     * @return String                      OCID of the queue
     */
    @Override
    public String createQueue(String queueName, String compartmentId, Integer deadLetterQueueDeliveryCount, Integer retentionInSeconds) {
        CreateQueueDetails details = CreateQueueDetails.builder().compartmentId(compartmentId).displayName(queueName)
                .deadLetterQueueDeliveryCount(deadLetterQueueDeliveryCount)
                .retentionInSeconds(retentionInSeconds).build();
        CreateQueueRequest request = CreateQueueRequest.builder().createQueueDetails(details).build();
        CreateQueueResponse response = queueAdminClient.createQueue(request);
        String requestId = response.getOpcWorkRequestId();
        System.out.println(" queue requested - request id : " + requestId);

        String queueId = null;
        GetWorkRequestRequest workRequestRequest = GetWorkRequestRequest.builder().workRequestId(requestId).build();
        Waiter<GetWorkRequestRequest, GetWorkRequestResponse> waiter = queueAdminClient.getWaiters()
                .forWorkRequest(workRequestRequest);
        try {
            waiter.execute();
            GetWorkRequestResponse workRequestResponse = queueAdminClient.getWorkRequest(workRequestRequest);
            WorkRequest workRequestData = workRequestResponse.getWorkRequest();
            queueId = workRequestData.getResources().get(0).getIdentifier();
        } catch (Exception e) {
            System.out.println(" queue build failed : " + e.getMessage());
        }
        return queueId;
    }

    /**
     * Get the queue resource.
     *
     * @param queueId OCID of the queue
     * @return GetQueueResponse
     */
    @Override
    public GetQueueResponse getQueue(String queueId) {
        GetQueueRequest request = GetQueueRequest.builder().queueId(queueId).build();
        GetQueueResponse response = queueAdminClient.getQueue(request);
        return response;
    }

    /**
     * List the queues by name and/or in a compartment.
     *
     * @param queueName     Name of the queue to list them
     * @param compartmentId Compartment OCID where to list the Queues
     * @return ListQueuesResponse
     */
    @Override
    public ListQueuesResponse listQueues(String queueName, String compartmentId) {
        ListQueuesRequest.Builder builder = ListQueuesRequest.builder().compartmentId(compartmentId);
        if (StringUtils.hasText(queueName)) {
            builder = builder.displayName(queueName);
        }
        ListQueuesResponse response = queueAdminClient.listQueues(builder.build());
        System.out.println("result queues : " + response.getQueueCollection().getItems());
        return response;
    }

    /**
     * Delete the queue by id.
     *
     * @param queueId OCID of the queue
     * @return DeleteQueueResponse
     */
    @Override
    public DeleteQueueResponse deleteQueue(String queueId) {
        DeleteQueueRequest request = DeleteQueueRequest.builder().queueId(queueId).build();
        DeleteQueueResponse response = queueAdminClient.deleteQueue(request);
        return response;
    }
}
