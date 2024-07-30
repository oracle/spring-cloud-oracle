/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.queue;

import com.oracle.bmc.queue.QueueAdminClient;
import com.oracle.bmc.queue.QueueClient;
import com.oracle.bmc.queue.responses.DeleteMessageResponse;
import com.oracle.bmc.queue.responses.DeleteQueueResponse;
import com.oracle.bmc.queue.responses.GetMessagesResponse;
import com.oracle.bmc.queue.responses.GetQueueResponse;
import com.oracle.bmc.queue.responses.ListQueuesResponse;
import com.oracle.bmc.queue.responses.PutMessagesResponse;
import com.oracle.bmc.queue.responses.UpdateMessagesResponse;

/**
 * Interface for defining OCI Queue module.
 */
public interface Queue {

    /**
     * Direct instance of OCI Java SDK QueueAdminClient.
     * @return QueueAdminClient
     */
    QueueAdminClient getQueueAdminClient();

    /**
     * Direct instance of OCI Java SDK QueueClient.
     * @return QueueClient
     */
    QueueClient getQueueClient();

    /**
     * Put messages into a queue.
     *
     * @param queueId  OCID of the queue
     * @param messages list of messages
     * @return PutMessagesResponse
     */
    PutMessagesResponse putMessages(String queueId, String[] messages);

    /**
     * Get/consume messages from a queue.
     *
     * @param queueId             OCID of the queue
     * @param visibilityInSeconds Messages will be hidden and won't be consumable by other consumers for this time
     * @param timeoutInSeconds    Timeout for the request
     * @param limit               The maximum number of messages returned
     * @return GetMessagesResponse
     */
    GetMessagesResponse getMessages(String queueId, Integer visibilityInSeconds, Integer timeoutInSeconds, Integer limit);

    /**
     * Update the messages by their receipt in a queue.
     *
     * @param queueId             OCID of the queue
     * @param messageReceipts     List of message receipts to be updated
     * @param visibilityInSeconds Messages will be hidden and won't be consumable by other consumers for this time
     * @return UpdateMessagesResponse
     */
    UpdateMessagesResponse updateMessages(String queueId, String[] messageReceipts, Integer visibilityInSeconds);

    /**
     * Delete the message by the receipt from a queue
     *
     * @param queueId        OCID of the queue
     * @param messageReceipt The receipt of the message to be deleted
     * @return DeleteMessageResponse
     */
    DeleteMessageResponse deleteMessage(String queueId, String messageReceipt);

    /**
     * Creates a queue in a compartment.
     *
     * @param queueName                    Name of the queue to be created
     * @param compartmentId                Compartment OCID where the Queue needs to be created
     * @param deadLetterQueueDeliveryCount The number of times a message is delivered before its moved to the dead letter queue
     * @param retentionInSeconds           The time a message remains in the queue before its deleted by the service if it is not deleted by a consumer
     * @return String                      OCID of the queue
     */
    String createQueue(String queueName, String compartmentId, Integer deadLetterQueueDeliveryCount, Integer retentionInSeconds);

    /**
     * Get the queue resource.
     *
     * @param queueId OCID of the queue
     * @return GetQueueResponse
     */
    GetQueueResponse getQueue(String queueId);

    /**
     * List the queues by name and/or in a compartment.
     *
     * @param queueName     Name of the queue to list them
     * @param compartmentId Compartment OCID where to list the Queues
     * @return ListQueuesResponse
     */
    ListQueuesResponse listQueues(String queueName, String compartmentId);

    /**
     * Delete the queue by id.
     *
     * @param queueId OCID of the queue
     * @return DeleteQueueResponse
     */
    DeleteQueueResponse deleteQueue(String queueId);
}
