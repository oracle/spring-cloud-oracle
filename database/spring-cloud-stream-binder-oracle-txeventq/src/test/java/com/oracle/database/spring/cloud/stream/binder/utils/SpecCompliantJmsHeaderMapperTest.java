// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.database.spring.cloud.stream.binder.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.jms.Message;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpecCompliantJmsHeaderMapperTest {

    @Test
    void preservesApplicationAndPartitionMetadata() throws Exception {
        Map<String, Object> headers = new SpecCompliantJmsHeaderMapper().toHeaders(message(Map.of(
                "content_type", "text/plain",
                "correlationId", "group-1",
                "sequenceNumber", 2,
                "sequenceSize", 3,
                "custom_header", "application-value")));

        assertThat(headers).containsEntry("contentType", "text/plain")
                .containsEntry("correlationId", "group-1")
                .containsEntry("sequenceNumber", 2)
                .containsEntry("sequenceSize", 3)
                .containsEntry("custom_header", "application-value");
    }

    @Test
    void rejectsRemoteRoutingAndJsonTypeHeaders() throws Exception {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("replyChannel", "privilegedChannel");
        properties.put("errorChannel", "privilegedChannel");
        properties.put("routingSlip", "privilegedChannel");
        properties.put("json__TypeId__", "untrusted.Type");
        properties.put("json__ContentTypeId__", "untrusted.Element");
        properties.put("json__KeyTypeId__", "untrusted.Key");
        properties.put("custom_header", "application-value");

        Map<String, Object> headers = new SpecCompliantJmsHeaderMapper().toHeaders(message(properties));

        assertThat(headers).doesNotContainKeys("replyChannel", "errorChannel", "routingSlip",
                "json__TypeId__", "json__ContentTypeId__", "json__KeyTypeId__")
                .containsEntry("custom_header", "application-value");
    }

    private Message message(Map<String, Object> properties) throws Exception {
        Message message = mock(Message.class);
        when(message.getPropertyNames()).thenReturn(Collections.enumeration(properties.keySet()));
        when(message.getObjectProperty(anyString())).thenAnswer(call -> properties.get(call.getArgument(0)));
        return message;
    }
}
