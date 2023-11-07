/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.queue.springcloudociqueuesample;

import com.oracle.bmc.queue.responses.*;
import com.oracle.cloud.spring.core.util.OCIObjectMapper;
import com.oracle.cloud.spring.queue.Queue;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("demoapp/api/queues")
@Tag(name = "Queue APIs")
public class QueueController {

    @Autowired
    Queue queue;

    @PostMapping(value = "/{queueId}/messages")
    ResponseEntity<?> putMessages(@Parameter(required = true, example = "queueId") @PathVariable String queueId,
                                  @Parameter(required = true, example = "messages") @RequestParam String[] messages) {
        PutMessagesResponse response = queue.putMessages(queueId, messages);
        return ResponseEntity.ok().body("result messages : " + OCIObjectMapper.toPrintableString(response.getPutMessages().getMessages()));
    }

    @GetMapping(value = "/{queueId}/messages")
    ResponseEntity<?> getMessages(@Parameter(required = true, example = "queueId") @PathVariable String queueId,
                                  @Parameter(example = "visibilityInSeconds") @RequestParam Integer visibilityInSeconds,
                                  @Parameter(example = "timeoutInSeconds") @RequestParam Integer timeoutInSeconds,
                                  @Parameter(example = "limit") @RequestParam Integer limit) {
        GetMessagesResponse response = queue.getMessages(queueId, visibilityInSeconds, timeoutInSeconds, limit);
        return ResponseEntity.ok().body("get messages : " + OCIObjectMapper.toPrintableString(response.getGetMessages().getMessages()));
    }

    @PutMapping(value = "/{queueId}/messages")
    ResponseEntity<?> updateMessages(@Parameter(required = true, example = "queueId") @PathVariable String queueId,
                                     @Parameter(required = true, example = "messageReceipts") @RequestParam String[] messageReceipts,
                                     @Parameter(example = "visibilityInSeconds") @RequestParam Integer visibilityInSeconds) {
        UpdateMessagesResponse response = queue.updateMessages(queueId, messageReceipts, visibilityInSeconds);
        return ResponseEntity.ok().body("updated messages : " + OCIObjectMapper.toPrintableString(response.getUpdateMessagesResult().getEntries()));
    }

    @DeleteMapping(value = "/{queueId}/messages")
    ResponseEntity<?> deleteMessage(@Parameter(required = true, example = "queueId") @PathVariable String queueId,
                                    @Parameter(required = true, example = "messageReceipt") @RequestParam String messageReceipt) {
        DeleteMessageResponse response = queue.deleteMessage(queueId, messageReceipt);
        return ResponseEntity.ok().body("opcRequestId for deleting the message : " + response.getOpcRequestId());
    }

    @PostMapping
    ResponseEntity<?> createQueue(@Parameter(required = true, example = "queueName") @RequestParam String queueName,
                                  @Parameter(required = true, example = "compartmentId") @RequestParam String compartmentId,
                                  @Parameter(example = "deadLetterQueueDeliveryCount") @RequestParam Integer deadLetterQueueDeliveryCount,
                                  @Parameter(example = "retentionInSeconds") @RequestParam Integer retentionInSeconds) {
        String queueId = queue.createQueue(queueName, compartmentId, deadLetterQueueDeliveryCount, retentionInSeconds);
        return ResponseEntity.accepted().body("queue id : " + queueId);
    }

    @GetMapping(value = "/{queueId}")
    ResponseEntity<?> getQueue(@Parameter(required = true, example = "queueId") @PathVariable String queueId) {
        GetQueueResponse response = queue.getQueue(queueId);
        return ResponseEntity.ok().body("queue : " + OCIObjectMapper.toPrintableString(response.getQueue()));
    }

    @GetMapping
    ResponseEntity<?> listQueues(@Parameter(example = "queueName") @RequestParam String queueName,
                                 @Parameter(example = "compartmentId") @RequestParam String compartmentId) {
        ListQueuesResponse response = queue.listQueues(queueName, compartmentId);
        return ResponseEntity.ok().body("queues : " + OCIObjectMapper.toPrintableString(response.getQueueCollection().getItems()));
    }

    @DeleteMapping(value = "/{queueId}")
    ResponseEntity<?> deleteQueue(@Parameter(required = true, example = "queueId") @PathVariable String queueId) {
        DeleteQueueResponse response = queue.deleteQueue((queueId));
        return ResponseEntity.ok().body("opcRequestId for deleting the queue : " + response.getOpcRequestId());
    }
}
