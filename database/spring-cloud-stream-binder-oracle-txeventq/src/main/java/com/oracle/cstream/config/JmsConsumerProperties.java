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

package com.oracle.cstream.config;

public class JmsConsumerProperties {
	private static final String DEFAULT_DLQ_NAME = "Spring_Cloud_Stream_dlq";

	/* Properties relevant for batching of messages */
	private int batchSize = 10;
	
	private int timeout = 1000;  // in milliseconds, default => 1 second
	
	/** the name of the dead letter queue **/
	private String dlqName = DEFAULT_DLQ_NAME;
	
	private String deSerializer = null;

	public String getDeSerializer() {
		return deSerializer;
	}

	public void setDeSerializer(String deSerializer) {
		this.deSerializer = deSerializer;
	}

	public String getDlqName() {
		return dlqName;
	}

	public void setDlqName(String dlqName) {
		this.dlqName = dlqName;
	}

	public int getTimeout() {
		return this.timeout;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
