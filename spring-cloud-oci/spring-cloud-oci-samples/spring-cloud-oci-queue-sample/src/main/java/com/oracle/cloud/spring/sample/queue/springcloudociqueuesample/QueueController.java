/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.queue.springcloudociqueuesample;

import com.oracle.bmc.queue.responses.DeleteMessageResponse;
import com.oracle.bmc.queue.responses.DeleteQueueResponse;
import com.oracle.bmc.queue.responses.GetMessagesResponse;
import com.oracle.bmc.queue.responses.GetQueueResponse;
import com.oracle.bmc.queue.responses.ListQueuesResponse;
import com.oracle.bmc.queue.responses.PutMessagesResponse;
import com.oracle.bmc.queue.responses.UpdateMessagesResponse;
import com.oracle.cloud.spring.core.util.OCIObjectMapper;
import com.oracle.cloud.spring.queue.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demoapp/api/queues")
public class QueueController {

    @Autowired
    Queue queue;

    @PostMapping(value = "/{queueId}/messages")
    ResponseEntity<?> putMessages(@PathVariable String queueId,
                                  @RequestParam String[] messages) {
        PutMessagesResponse response = queue.putMessages(queueId, messages);
        return ResponseEntity.ok().body("result messages : " + OCIObjectMapper.toPrintableString(response.getPutMessages().getMessages()));
    }

    @GetMapping(value = "/{queueId}/messages")
    ResponseEntity<?> getMessages(@PathVariable String queueId,
                                  @RequestParam Integer visibilityInSeconds,
                                  @RequestParam Integer timeoutInSeconds,
                                  @RequestParam Integer limit) {
        GetMessagesResponse response = queue.getMessages(queueId, visibilityInSeconds, timeoutInSeconds, limit);
        return ResponseEntity.ok().body("get messages : " + OCIObjectMapper.toPrintableString(response.getGetMessages().getMessages()));
    }

    @PutMapping(value = "/{queueId}/messages")
    ResponseEntity<?> updateMessages(@PathVariable String queueId,
                                     @RequestParam String[] messageReceipts,
                                     @RequestParam Integer visibilityInSeconds) {
        UpdateMessagesResponse response = queue.updateMessages(queueId, messageReceipts, visibilityInSeconds);
        return ResponseEntity.ok().body("updated messages : " + OCIObjectMapper.toPrintableString(response.getUpdateMessagesResult().getEntries()));
    }

    @DeleteMapping(value = "/{queueId}/messages")
    ResponseEntity<?> deleteMessage(@PathVariable String queueId,
                                    @RequestParam String messageReceipt) {
        DeleteMessageResponse response = queue.deleteMessage(queueId, messageReceipt);
        return ResponseEntity.ok().body("opcRequestId for deleting the message : " + response.getOpcRequestId());
    }

    @PostMapping
    ResponseEntity<?> createQueue(@RequestParam String queueName,
                                  @RequestParam String compartmentId,
                                  @RequestParam Integer deadLetterQueueDeliveryCount,
                                  @RequestParam Integer retentionInSeconds) {
        String queueId = queue.createQueue(queueName, compartmentId, deadLetterQueueDeliveryCount, retentionInSeconds);
        return ResponseEntity.accepted().body("queue id : " + queueId);
    }

    @GetMapping(value = "/{queueId}")
    ResponseEntity<?> getQueue(@PathVariable String queueId) {
        GetQueueResponse response = queue.getQueue(queueId);
        return ResponseEntity.ok().body("queue : " + OCIObjectMapper.toPrintableString(response.getQueue()));
    }

    @GetMapping
    ResponseEntity<?> listQueues(@RequestParam String queueName,
                                 @RequestParam String compartmentId) {
        ListQueuesResponse response = queue.listQueues(queueName, compartmentId);
        return ResponseEntity.ok().body("queues : " + OCIObjectMapper.toPrintableString(response.getQueueCollection().getItems()));
    }

    @DeleteMapping(value = "/{queueId}")
    ResponseEntity<?> deleteQueue(@PathVariable String queueId) {
        DeleteQueueResponse response = queue.deleteQueue((queueId));
        return ResponseEntity.ok().body("opcRequestId for deleting the queue : " + response.getOpcRequestId());
    }
}
