/*
 ** TxEventQ Support for Spring Cloud Stream
 ** Copyright (c) 2023, 2024 Oracle and/or its affiliates.
 **
 ** This file has been modified by Oracle Corporation.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oracle.database.cstream.provisioning;

import jakarta.jms.JMSException;
import jakarta.jms.Topic;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.jms.support.JmsUtils;

public class JmsProducerDestination implements ProducerDestination {

    private final Topic topic;
    private final int partitionCount;
    private final int dbversion;

    public JmsProducerDestination(Topic topic, int pCount, int dbversion) {
        this.topic = topic;
        this.partitionCount = pCount;
        this.dbversion = dbversion;
    }

    @Override
    public String getName() {
        try {
            return topic.getTopicName();
        } catch (JMSException e) {
            throw new ProvisioningException(
                    "Error getting topic name",
                    JmsUtils.convertJmsAccessException(e)
            );
        }
    }

    @Override
    public String getNameForPartition(int partition) {
        try {
            return topic.getTopicName();
        } catch (JMSException e) {
            throw new ProvisioningException(
                    "Error getting topic name",
                    JmsUtils.convertJmsAccessException(e)
            );
        }
    }

    public int getDBVersion() {
        return this.dbversion;
    }

    @Override
    public String toString() {
        return (
                "JmsProducerDestination{" + "topic=" + topic + ", partitions=" + partitionCount + ", DB Version: " + this.dbversion + "}"
        );
    }
}
