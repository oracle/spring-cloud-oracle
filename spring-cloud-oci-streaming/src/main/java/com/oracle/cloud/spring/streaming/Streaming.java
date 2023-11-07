/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.streaming;

import com.oracle.bmc.streaming.Stream;
import com.oracle.bmc.streaming.Stream;
import com.oracle.bmc.streaming.StreamAdmin;
import com.oracle.bmc.streaming.model.CreateCursorDetails;
import com.oracle.bmc.streaming.model.CreateGroupCursorDetails;
import com.oracle.bmc.streaming.model.PutMessagesDetails;
import com.oracle.bmc.streaming.requests.CreateStreamPoolRequest;
import com.oracle.bmc.streaming.requests.CreateStreamRequest;
import com.oracle.bmc.streaming.requests.DeleteStreamPoolRequest;
import com.oracle.bmc.streaming.requests.DeleteStreamRequest;
import com.oracle.bmc.streaming.responses.*;

import java.util.Date;
import java.util.List;

/**
 * Interface for defining the OCI streaming module.
 */
public interface Streaming {

    /**
     * Direct instance of OCI Java SDK streaming Client.
     * @return Stream
     */
    Stream getClient();

    /**
     * Direct instance of OCI Java SDK streaming Admin Client.
     * @return StreamAdmin
     */
    StreamAdmin getAdminClient();

    /**
     * creates stream resource in OCI
     * @param name name of the stream
     * @param streamPoolId OCID of the stream pool
     * @param partitions number of partitions
     * @param retentionInHours retention In Hours for messages in the stream
     * @return CreateStreamResponse
     */
    CreateStreamResponse createStream(String name, String streamPoolId, Integer partitions,
                                      Integer retentionInHours);

    /**
     * creates stream pool resource in OCI
     * @param name name of the stream
     * @param compartmentId OCID of the compartment
     * @return CreateStreamPoolResponse
     */
    CreateStreamPoolResponse createStreamPool(String name, String compartmentId);

    /**
     * deletes stream resource from OCI
     * @param streamId OCID of the stream resource
     * @return DeleteStreamResponse
     */
    DeleteStreamResponse deleteStream(String streamId);

    /**
     * deletes stream pool resource from OCI
     * @param streamPoolId OCID of the stream pool resource
     * @return DeleteStreamResponse
     */
    DeleteStreamPoolResponse deleteStreamPool(String streamPoolId);

    /**
     * Ingests stream message associated with a Stream OCID
     * @param streamId OCID of the stream resource
     * @param key Key of the content of the stream message to be ingested
     * @param values list of values of the content of the stream message to be ingested for a corresponding key
     * @return PutMessagesResponse
     */
    PutMessagesResponse putMessages(String streamId, byte[] key, List<byte[]> values);

    /**
     * Ingests stream message associated with a Stream OCID
     * @param streamId OCID of the stream resource
     * @param putMessagesDetails Content of the stream message to be ingested
     * @return PutMessagesResponse
     */
    PutMessagesResponse putMessages(String streamId, PutMessagesDetails putMessagesDetails);

    /**
     * Retrieves stream message associated with a Stream OCID
     * @param streamId OCID of the stream resource
     * @param cursor its cursor which determines the starting point from which the stream will be consumed.
     * @return GetMessagesResponse
     */
    GetMessagesResponse getMessages(String streamId, String cursor);

    /**
     * Creates cursor point from where Content of the stream message to be retrieved
     * @param streamId OCID of the stream resource
     * @param offset The offset to consume from if the cursor type is AT_OFFSET or AFTER_OFFSET.
     * @param time The time to consume from if the cursor type is AT_TIME, expressed in RFC 3339 timestamp format.
     * @param type The type of cursor, which determines the starting point from which the stream will be consumed.
     * @param partition The partition to get messages from.
     * @return CreateCursorResponse
     */
    CreateCursorResponse createCursor(String streamId, Long offset, Date time, CreateCursorDetails.Type type, String partition);

    /**
     * Creates cursor point from where Content of the stream message to be retrieved
     * @param streamId OCID of the stream resource
     * @param groupName Name of the consumer group.
     * @param time The time to consume from if type is AT_TIME.
     * @param type The type of cursor, which determines the starting point from which the stream will be consumed.
     * @param commitOnGet When using consumer-groups, the default commit-on-get behaviour can be overriden by setting this value to false.
     * @param instanceName A unique identifier for the instance joining the consumer group.
     * @param timeoutInMs The amount of a consumer instance inactivity time, before partition reservations are released.
     * @return CreateGroupCursorResponse
     */
    CreateGroupCursorResponse createGroupCursor(String streamId, String groupName, Date time, CreateGroupCursorDetails.Type type,
                                                Boolean commitOnGet, String instanceName, Integer timeoutInMs);
}
