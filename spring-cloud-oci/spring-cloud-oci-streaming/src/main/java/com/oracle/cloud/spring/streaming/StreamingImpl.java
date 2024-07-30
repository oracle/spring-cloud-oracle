/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.streaming;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.oracle.bmc.streaming.Stream;
import com.oracle.bmc.streaming.StreamAdmin;
import com.oracle.bmc.streaming.model.CreateCursorDetails;
import com.oracle.bmc.streaming.model.CreateGroupCursorDetails;
import com.oracle.bmc.streaming.model.CreateStreamDetails;
import com.oracle.bmc.streaming.model.CreateStreamPoolDetails;
import com.oracle.bmc.streaming.model.PutMessagesDetails;
import com.oracle.bmc.streaming.model.PutMessagesDetailsEntry;
import com.oracle.bmc.streaming.requests.CreateCursorRequest;
import com.oracle.bmc.streaming.requests.CreateGroupCursorRequest;
import com.oracle.bmc.streaming.requests.CreateStreamPoolRequest;
import com.oracle.bmc.streaming.requests.CreateStreamRequest;
import com.oracle.bmc.streaming.requests.DeleteStreamPoolRequest;
import com.oracle.bmc.streaming.requests.DeleteStreamRequest;
import com.oracle.bmc.streaming.requests.GetMessagesRequest;
import com.oracle.bmc.streaming.requests.PutMessagesRequest;
import com.oracle.bmc.streaming.responses.CreateCursorResponse;
import com.oracle.bmc.streaming.responses.CreateGroupCursorResponse;
import com.oracle.bmc.streaming.responses.CreateStreamPoolResponse;
import com.oracle.bmc.streaming.responses.CreateStreamResponse;
import com.oracle.bmc.streaming.responses.DeleteStreamPoolResponse;
import com.oracle.bmc.streaming.responses.DeleteStreamResponse;
import com.oracle.bmc.streaming.responses.GetMessagesResponse;
import com.oracle.bmc.streaming.responses.PutMessagesResponse;

/**
 * Implementation of the OCI streaming module.
 */
public class StreamingImpl implements Streaming {

    private final Stream stream;
    private final StreamAdmin streamAdmin;
    public StreamingImpl(Stream stream, StreamAdmin streamAdmin) {
        this.stream = stream;
        this.streamAdmin = streamAdmin;
    }

    /**
     * creates stream resource in OCI
     * @param name name of the stream
     * @param streamPoolId OCID of the stream pool
     * @param partitions number of partitions
     * @param retentionInHours retention In Hours for messages in the stream
     * @return CreateStreamResponse
     */
    public CreateStreamResponse createStream(String name, String streamPoolId, Integer partitions,
                                      Integer retentionInHours) {
        CreateStreamRequest.Builder requestBuilder = CreateStreamRequest.builder();

        CreateStreamDetails.Builder createStreamDetailsBuilder = CreateStreamDetails.builder();
        createStreamDetailsBuilder.streamPoolId(streamPoolId).name(name);
        if (partitions != null)  createStreamDetailsBuilder.partitions(partitions);
        if (retentionInHours != null)  createStreamDetailsBuilder.retentionInHours(retentionInHours);

        CreateStreamDetails createStreamDetails = createStreamDetailsBuilder.build();

        requestBuilder.createStreamDetails(createStreamDetails);
        CreateStreamRequest request = requestBuilder.build();
        CreateStreamResponse response = streamAdmin.createStream(request);
        return response;
    }

    /**
     * creates stream pool resource in OCI
     * @param name name of the stream
     * @param compartmentId OCID of the compartment
     * @return CreateStreamPoolResponse
     */
    public CreateStreamPoolResponse createStreamPool(String name, String compartmentId) {
        CreateStreamPoolRequest.Builder requestBuilder = CreateStreamPoolRequest.builder();
        CreateStreamPoolDetails.Builder  createStreamPoolDetailsBuilder = CreateStreamPoolDetails.builder();
        createStreamPoolDetailsBuilder.name(name).compartmentId(compartmentId);

        CreateStreamPoolDetails createStreamPoolDetails = createStreamPoolDetailsBuilder.build();
        requestBuilder.createStreamPoolDetails(createStreamPoolDetails);
        CreateStreamPoolRequest request = requestBuilder.build();
        CreateStreamPoolResponse response = streamAdmin.createStreamPool(request);
        return response;
    }

    /**
     * deletes stream resource from OCI
     * @param streamId OCID of the stream resource
     * @return DeleteStreamResponse
     */
    public DeleteStreamResponse deleteStream(String streamId) {
        DeleteStreamRequest request = DeleteStreamRequest.builder().streamId(streamId).build();
        DeleteStreamResponse response = streamAdmin.deleteStream(request);
        return response;
    }

    /**
     * deletes stream pool resource from OCI
     * @param streamPoolId OCID of the stream pool resource
     * @return DeleteStreamResponse
     */
    public DeleteStreamPoolResponse deleteStreamPool(String streamPoolId) {
        DeleteStreamPoolRequest request = DeleteStreamPoolRequest.builder().streamPoolId(streamPoolId).build();
        DeleteStreamPoolResponse response = streamAdmin.deleteStreamPool(request);
        return response;
    }

    /**
     * Direct instance of OCI Java SDK streaming Client.
     * @return Stream
     */
    @Override
    public Stream getClient() {
        return stream;
    }

    /**
     * Direct instance of OCI Java SDK streaming Admin Client.
     * @return StreamAdmin
     */
    @Override
    public StreamAdmin getAdminClient() {
        return streamAdmin;
    }

    /**
     * Ingests stream message associated with a Stream OCID
     * @param streamId OCID of the stream resource
     * @param key Key of the content of the stream message to be ingested
     * @param values list of values of the content of the stream message to be ingested for a corresponding key
     * @return PutMessagesResponse
     */
    @Override
    public PutMessagesResponse putMessages(String streamId, byte[] key, List<byte[]> values) {

        PutMessagesRequest.Builder requestBuilder = PutMessagesRequest.builder();
        requestBuilder.streamId(streamId);

        List<PutMessagesDetailsEntry> messages = new ArrayList<>();

        if (values == null) {
            PutMessagesDetailsEntry entry = PutMessagesDetailsEntry.builder().key(key).value(null).build();
            messages.add(entry);
        } else {
            for (byte[] value : values) {
                PutMessagesDetailsEntry entry = PutMessagesDetailsEntry.builder().key(key).value(value).build();
                messages.add(entry);
            }
        }

        PutMessagesDetails putMessagesDetails = PutMessagesDetails.builder().messages(messages).build();

        requestBuilder.putMessagesDetails(putMessagesDetails);

        PutMessagesRequest request = requestBuilder.build();
        PutMessagesResponse response = stream.putMessages(request);
        return response;
    }

    /**
     * Ingests stream message associated with a Stream OCID
     * @param streamId OCID of the stream resource
     * @param putMessagesDetails Content of the stream message to be ingested
     * @return PutMessagesResponse
     */
    @Override
    public PutMessagesResponse putMessages(String streamId, PutMessagesDetails putMessagesDetails) {
        PutMessagesRequest.Builder requestBuilder = PutMessagesRequest.builder();
        requestBuilder.streamId(streamId);

        requestBuilder.putMessagesDetails(putMessagesDetails);

        PutMessagesRequest request = requestBuilder.build();
        PutMessagesResponse response = stream.putMessages(request);
        return response;
    }

    /**
     * Retrieves stream message associated with a Stream OCID
     * @param streamId OCID of the stream resource
     * @param cursor its cursor which determines the starting point from which the stream will be consumed.
     * @return GetMessagesResponse
     */
    @Override
    public GetMessagesResponse getMessages(String streamId, String cursor) {

        GetMessagesRequest.Builder requestBuilder = GetMessagesRequest.builder();
        requestBuilder.streamId(streamId);
        if (cursor != null) requestBuilder.cursor(cursor);

        GetMessagesRequest request = requestBuilder.build();
        GetMessagesResponse response = stream.getMessages(request);
        return response;
    }

    /**
     * Creates a cursor point from where Content of the stream message to be retrieved
     * @param streamId OCID of the stream resource
     * @param offset The offset to consume from if the cursor type is AT_OFFSET or AFTER_OFFSET.
     * @param time The time to consume from if the cursor type is AT_TIME, expressed in RFC 3339 timestamp format.
     * @param type The type of cursor, which determines the starting point from which the stream will be consumed.
     * @param partition The partition to get messages from.
     * @return CreateCursorResponse
     */
    @Override
    public CreateCursorResponse createCursor(String streamId, Long offset, Date time, CreateCursorDetails.Type type, String partition) {

        CreateCursorRequest.Builder requestBuilder = CreateCursorRequest.builder();
        requestBuilder.streamId(streamId);

        CreateCursorDetails.Builder createCursorDetailsBuilder = CreateCursorDetails.builder();
        if (offset != null) createCursorDetailsBuilder.offset(offset);
        if (time != null) createCursorDetailsBuilder.time(time);
        if (type != null) createCursorDetailsBuilder.type(type);
        if (partition != null) createCursorDetailsBuilder.partition(partition);

        requestBuilder.createCursorDetails(createCursorDetailsBuilder.build());
        CreateCursorRequest request = requestBuilder.build();
        CreateCursorResponse response = stream.createCursor(request);
        return response;
    }

    /**
     * Creates a group-cursor point from where Content of the stream message to be retrieved
     * @param streamId OCID of the stream resource
     * @param groupName Name of the consumer group.
     * @param time The time to consume from if type is AT_TIME.
     * @param type The type of cursor, which determines the starting point from which the stream will be consumed.
     * @param commitOnGet When using consumer-groups, the default commit-on-get behaviour can be overriden by setting this value to false.
     * @param instanceName A unique identifier for the instance joining the consumer group.
     * @param timeoutInMs The amount of a consumer instance inactivity time, before partition reservations are released.
     * @return CreateGroupCursorResponse
     */
    @Override
    public CreateGroupCursorResponse createGroupCursor(String streamId, String groupName, Date time, CreateGroupCursorDetails.Type type,
                                                  Boolean commitOnGet, String instanceName, Integer timeoutInMs) {

        CreateGroupCursorRequest.Builder requestBuilder = CreateGroupCursorRequest.builder();
        requestBuilder.streamId(streamId);

        CreateGroupCursorDetails.Builder createGroupCursorDetailsBuilder = CreateGroupCursorDetails.builder();
        if (time != null) createGroupCursorDetailsBuilder.time(time);
        if (groupName != null) createGroupCursorDetailsBuilder.groupName(groupName);
        if (type != null) createGroupCursorDetailsBuilder.type(type);
        if (commitOnGet != null) createGroupCursorDetailsBuilder.commitOnGet(commitOnGet);
        if (instanceName != null) createGroupCursorDetailsBuilder.instanceName(instanceName);
        if (timeoutInMs != null) createGroupCursorDetailsBuilder.timeoutInMs(timeoutInMs);

        requestBuilder.createGroupCursorDetails(createGroupCursorDetailsBuilder.build());
        CreateGroupCursorRequest request = requestBuilder.build();
        CreateGroupCursorResponse response = stream.createGroupCursor(request);
        return response;
    }
}
