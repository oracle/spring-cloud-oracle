/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.streaming;

import com.oracle.bmc.streaming.Stream;
import com.oracle.bmc.streaming.StreamAdmin;
import com.oracle.bmc.streaming.model.CreateCursorDetails;
import com.oracle.bmc.streaming.model.CreateGroupCursorDetails;
import com.oracle.bmc.streaming.model.PutMessagesDetails;
import com.oracle.bmc.streaming.responses.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StreamingImplTests {
    final StreamAdmin streamAdmin = mock(StreamAdmin.class);

    final Stream stream = mock(Stream.class);

    final Streaming streaming = new StreamingImpl(stream, streamAdmin);
    @Test
    void testStreamingAdminClient() {
        when(streamAdmin.createStreamPool(any())).thenReturn(mock(CreateStreamPoolResponse.class));
        when(streamAdmin.createStream(any())).thenReturn(mock(CreateStreamResponse.class));
        when(streamAdmin.deleteStreamPool(any())).thenReturn(mock(DeleteStreamPoolResponse.class));
        when(streamAdmin.deleteStream(any())).thenReturn(mock(DeleteStreamResponse.class));
        CreateStreamPoolResponse createStreamPool = streaming.createStreamPool("test","compartmentId");
        assertNotNull(createStreamPool);
        CreateStreamResponse createStream = streaming.createStream("test","poolId", 1,
                24);
        assertNotNull(createStream);
        DeleteStreamPoolResponse deleteStreamPool = streaming.deleteStreamPool("poolId");
        assertNotNull(deleteStreamPool);
        DeleteStreamResponse deleteStream = streaming.deleteStream("streamId");
        assertNotNull(deleteStream);
        assertNotNull(streaming.getAdminClient());
    }

    @Test
    void testStreamingClient() {
        when(stream.putMessages(any())).thenReturn(mock(PutMessagesResponse.class));
        when(stream.getMessages(any())).thenReturn(mock(GetMessagesResponse.class));
        when(stream.createCursor(any())).thenReturn(mock(CreateCursorResponse.class));
        when(stream.createGroupCursor(any())).thenReturn(mock(CreateGroupCursorResponse.class));
        PutMessagesResponse putMessages = streaming.putMessages("streamId", "key".getBytes(),
                Arrays.asList("value".getBytes()));
        assertNotNull(putMessages);
        PutMessagesResponse putMessagesBulk = streaming.putMessages("streamId", mock(PutMessagesDetails.class));
        assertNotNull(putMessagesBulk);
        GetMessagesResponse getMessages = streaming.getMessages("streamId", "cursor");
        assertNotNull(getMessages);
        CreateCursorResponse createCursor = streaming.createCursor("streamId", 0L, new Date(),
                CreateCursorDetails.Type.Latest, "0");
        assertNotNull(createCursor);
        CreateGroupCursorResponse createGroupCursor = streaming.createGroupCursor("streamId", "group",
                new Date(), CreateGroupCursorDetails.Type.Latest, false, "consumer1", 1000);
        assertNotNull(createGroupCursor);
        assertNotNull(streaming.getClient());
    }
}
