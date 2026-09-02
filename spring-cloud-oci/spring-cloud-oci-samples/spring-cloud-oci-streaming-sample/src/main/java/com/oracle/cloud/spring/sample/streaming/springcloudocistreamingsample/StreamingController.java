/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.streaming.springcloudocistreamingsample;

import com.oracle.bmc.streaming.model.CreateCursorDetails;
import com.oracle.bmc.streaming.model.CreateGroupCursorDetails;
import com.oracle.bmc.streaming.model.Cursor;
import com.oracle.bmc.streaming.model.Message;
import com.oracle.bmc.streaming.responses.CreateCursorResponse;
import com.oracle.bmc.streaming.responses.CreateGroupCursorResponse;
import com.oracle.bmc.streaming.responses.CreateStreamPoolResponse;
import com.oracle.bmc.streaming.responses.CreateStreamResponse;
import com.oracle.bmc.streaming.responses.DeleteStreamPoolResponse;
import com.oracle.bmc.streaming.responses.DeleteStreamResponse;
import com.oracle.bmc.streaming.responses.GetMessagesResponse;
import com.oracle.bmc.streaming.responses.PutMessagesResponse;
import com.oracle.cloud.spring.streaming.Streaming;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/demoapp/api/streaming/")
public class StreamingController {

    @Autowired
    Streaming streaming;

    @PostMapping(value = "createStreamPool")
    ResponseEntity<?> createStreamPool(@RequestParam String name,
                                  @RequestParam String compartmentId) {

        CreateStreamPoolResponse response = streaming.createStreamPool(name, compartmentId);
        return ResponseEntity.ok().body("Stream pool resource OCID : " + response.getStreamPool().getId());
    }

    @PostMapping(value = "createStream")
    ResponseEntity<?> createStream(@RequestParam String name,
                                  @RequestParam String streamPoolId,
                                  @RequestParam Integer partitions,
                                  @RequestParam Integer retentionInHours) {

        CreateStreamResponse response = streaming.createStream(name, streamPoolId, partitions, retentionInHours);
        return ResponseEntity.ok().body("Stream resource OCID : " + response.getStream().getId());
    }

    @DeleteMapping(value = "deleteStreamPool")
    ResponseEntity<?> deleteStreamPool(@RequestParam String streamPoolId) {

        DeleteStreamPoolResponse response = streaming.deleteStreamPool(streamPoolId);
        return ResponseEntity.ok().body("opc request Id for deleting the streamPool : " + response.getOpcRequestId());
    }
    @DeleteMapping(value = "deleteStream")
    ResponseEntity<?> deleteStream(@RequestParam String streamId) {

        DeleteStreamResponse response = streaming.deleteStream(streamId);
        return ResponseEntity.ok().body("opc request Id for deleting the stream : " + response.getOpcRequestId());
    }

    @PostMapping(value = "putMessages")
    ResponseEntity<?> putMessages(@RequestParam String streamId,
                                  @RequestParam (required = false) String key,
                                  @RequestParam (required = false) String value) {

        PutMessagesResponse response = streaming.putMessages(streamId, key.getBytes(), Arrays.asList(value.getBytes()));
        return ResponseEntity.ok().body("opc request Id for posting the messages to streaming : " + response.getOpcRequestId());
    }

    @PostMapping(value = "createCursor")
    ResponseEntity<?> createCursor(@RequestParam String streamId,
                                   @RequestParam (required = false) Long offset,
                                   @RequestParam (required = false) @DateTimeFormat(iso =
                                           DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                   @RequestParam (required = false) CreateCursorDetails.Type type,
                                   @RequestParam (required = false) String partition) {
        Date inputDate = null;
        if (time != null) {
            inputDate = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
        }
        CreateCursorResponse response = streaming.createCursor(streamId, offset, inputDate, type, partition);
        Cursor cursor = response.getCursor();
        String cursorValue = cursor.getValue();
        System.out.println(cursorValue);
        return ResponseEntity.ok().body("Cursor value is  : " + cursorValue);
    }

    @PostMapping(value = "createGroupCursor")
    ResponseEntity<?> createGroupCursor(@RequestParam String streamId,
                                        @RequestParam (required = false) String groupName,
                                        @RequestParam (required = false) @DateTimeFormat(iso =
                                                DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                        @RequestParam (required = false) CreateGroupCursorDetails.Type type,
                                        @RequestParam (required = false) Boolean commitOnGet,
                                        @RequestParam (required = false) String instanceName,
                                        @RequestParam (required = false) Integer timeoutInMs) {
        Date inputDate = null;
        if (time != null) {
            inputDate = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
        }
        CreateGroupCursorResponse response = streaming.createGroupCursor(streamId, groupName, inputDate, type, commitOnGet,
                instanceName, timeoutInMs);
        Cursor cursor = response.getCursor();
        String cursorValue = cursor.getValue();
        System.out.println(cursorValue);
        return ResponseEntity.ok().body("Group Cursor value is  : " + cursorValue);
    }

    @GetMapping(value = "getMessages")
    ResponseEntity<?> getMessages(@RequestParam String streamId,
                                  @RequestParam String cursor) {
        GetMessagesResponse response = streaming.getMessages(streamId, cursor);
        List<Message> messages = response.getItems();
        int size = messages.size();
        Message lastMessage = size > 0 ? messages.get(size-1) : null;
        String key = lastMessage == null ? null : new String(lastMessage.getKey());
        String value = lastMessage == null ? null : new String(lastMessage.getValue());
        return ResponseEntity.ok().body("Retrieved message (key,value) is : (" + key + ", " + value + ")");
    }
}
