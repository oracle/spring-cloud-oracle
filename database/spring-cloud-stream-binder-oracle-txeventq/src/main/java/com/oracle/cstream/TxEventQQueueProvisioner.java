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

package com.oracle.cstream;

import com.oracle.cstream.config.JmsConsumerProperties;
import com.oracle.cstream.config.JmsProducerProperties;
import com.oracle.cstream.plsql.OracleDBUtils;
import com.oracle.cstream.provisioning.JmsConsumerDestination;
import com.oracle.cstream.provisioning.JmsProducerDestination;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.Topic;

import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.jms.support.JmsUtils;

public class TxEventQQueueProvisioner
  implements
    ProvisioningProvider<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>> {

  private final ConnectionFactory connectionFactory;
  
  private final Logger logger = LoggerFactory.getLogger(TxEventQQueueProvisioner.class);

  private OracleDBUtils dbutils;
  
  public void setDBUtils(OracleDBUtils dbutils) {
	  this.dbutils = dbutils;
  }
  
  public TxEventQQueueProvisioner(
    ConnectionFactory connectionFactory,
    OracleDBUtils dbutils
  ) {
    this.connectionFactory = connectionFactory;
    this.dbutils = dbutils;
  }

  @Override
  public ProducerDestination provisionProducerDestination(
    final String name,
    ExtendedProducerProperties<JmsProducerProperties> properties
  ) {
	  // Step 1: Get correct topic name
	  String topicName = formatName(name);
	  logger.info("Binding topic {} for {}", topicName, properties.getBindingName());
	  
	  // Step 2: Provision topic for producer binding
	  // along with required groups (if any)
	  Topic topic = provisionProducerTopic(topicName, properties);
	  
	  // Step 3: Return the required ProducerDestination
	  return new JmsProducerDestination(topic, properties.getPartitionCount(), this.dbutils.getDBVersion());
  }

  @Override
  public ConsumerDestination provisionConsumerDestination(
    String name,
    String group,
    ExtendedConsumerProperties<JmsConsumerProperties> properties
  ) {
	  if(properties.isMultiplex()) {
		  throw new 
		  	IllegalArgumentException("The property 'multiplex:true' is not supported.");
	  }
	  if(properties.getInstanceIndexList() != null && 
			  !properties.getInstanceIndexList().isEmpty()) {
		  throw new 
		  	IllegalArgumentException("The property 'instanceIndexList' is not supported.");
	  }
	  
	  return provideSingleConsumerDestination(name, group, properties);
  }
  
  /* 
   * Helper function to bind one consumer destination name 
   */
  public ConsumerDestination provideSingleConsumerDestination(String name,
		  String group,
		  ExtendedConsumerProperties<JmsConsumerProperties> properties) {
	  	String topicName = formatName(name);
	    logger.info("Binding topic {} for {} for group {}", topicName, properties.getBindingName(), group);
	    
	    // Step 2: Provision topic for consumer binding
		provisionConsumerTopic(topicName, properties);
		  
		// Step 3: Return the required ConsumerDestination
		return new JmsConsumerDestination(topicName, this.dbutils.getDBVersion());		  
  }
  
  /*
   * Utility function to allocate necessary resources
   * 	for a producer destination with the configured properties
   * If the topic does not exist, it creates one with the configured
   * 	partition count. 
   * By default, if the producer is not partitioned, then
   * 	topic with only one partition is created.
   * Throws an error if topic exists and is partitioned
   * 	but the configured partitionCount does not match
   * 	actual partitions on the topic
   */
  private Topic provisionProducerTopic(String topicName, 
		  ExtendedProducerProperties<JmsProducerProperties> properties) {
	  	Connection aQConnection = null;
	    Session session = null;
	    Topic topic = null;
	    try {
	      aQConnection = connectionFactory.createConnection();
	      session = aQConnection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
	      
	      topic = getTopicInstance(topicName, session);
	      
	      if(topic == null) {
			  logger.info("Creating topic: {} as it does not exist.", topicName);
			  // create Key based TEQ with specified number of partitions
			  int partitionNum = properties.getPartitionCount();
			  createTopicWithPartitions(topicName, partitionNum);
			  topic = getTopicInstance(topicName, session);
		  } else if(properties.isPartitioned()){
			  // topic exists
			  // match its partition Count
			  int partitionNum = properties.getPartitionCount();
			  checkPartitionCount(topicName, partitionNum);
		  }
	      
	      // create necessary required subscriptions
	      for(String requiredGroup: properties.getRequiredGroups()) {
	    	  session.createDurableSubscriber(topic, requiredGroup);
	      }
	      
	      JmsUtils.commitIfNecessary(session);
	    } catch (JMSException e) {
	      throw new IllegalStateException(e);
	    } finally {
		      JmsUtils.closeSession(session);
		      JmsUtils.closeConnection(aQConnection);
	    }
	    
	    return topic;
  }
  
  private Topic provisionConsumerTopic(String topicName,
		  ExtendedConsumerProperties<JmsConsumerProperties> properties) {
	  	Connection aQConnection = null;
	    Session session = null;
	    Topic topic = null;
	    try {
	      aQConnection = connectionFactory.createConnection();
	      session = aQConnection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
	      
	      topic = getTopicInstance(topicName, session);
	      
	      if(topic == null) {
			  logger.info("Creating topic: {} as it does not exist.", topicName);
			  createTopicWithInstances(topicName, properties);
			  topic = getTopicInstance(topicName, session);
		  } else if(properties.isPartitioned()){
			  // topic exists
			  // match its instance Count
			  int instCnt = properties.getInstanceCount();
			  checkPartitionCount(topicName, instCnt);
		  }
	      
	      /* Set instance index to -1 if not partitioned */
	      if(!properties.isPartitioned()) properties.setInstanceIndex(-1);
	      
	      JmsUtils.commitIfNecessary(session);
	    } catch (JMSException e) {
	      throw new IllegalStateException(e);
	    } finally {
		      JmsUtils.closeSession(session);
		      JmsUtils.closeConnection(aQConnection);
	    }
	    
	    return topic;
  }
  
  private void createTopicWithPartitions(String topicName, int pCount) {
	  try {
			this.dbutils.createKBQ(topicName, pCount);
	  } catch (SQLException e) {
			throw new IllegalArgumentException("Error when creating procedures & topic.", e);
	  }
  }
  
  private void createTopicWithInstances(String topicName, 
		  ExtendedConsumerProperties<JmsConsumerProperties> properties) {
	  try {
		  if(properties.isPartitioned()) {
			  // the consumer is partitioned
			  // create KBQ with configured instance count
			  int instCnt = properties.getInstanceCount();
			  this.dbutils.createKBQ(topicName, instCnt);
	  	  }
	  	  else {
	  		  // The consumer is not partitioned
	  		  // create default KBQ with 1 partition
	  		  this.dbutils.createKBQ(topicName, 1);
	  	  }
	  }
	  catch (SQLException e) {
			throw new IllegalArgumentException("Error when creating topic.", e);
	  }
  }
  
  private void checkPartitionCount(String topicName, int pCount) {
	  try {
		  int topicParts = this.dbutils.getTopicPartitions(topicName);
		  if(pCount != topicParts) {
			  throw new IllegalArgumentException("Partition Count mismatch, Expected: " 
					  								+ topicParts + ", Found: " + pCount);
		  }
	  } catch(SQLException e) {
		  throw new IllegalArgumentException("Error when checking partitionCount on topics.", e);
	  }
  }
  
  
  
  private String formatName(String name) {
	  // surround with double quotes 
	  // to use exact name for topic
	  return "\"" + name + "\"";
  }
  
  /* Utility function to check if the topic exists or not
   * If the topic does not exist, returns null
   * Otherwise returns the actual Topic object 
   * associated with the name topicName
   */
  private Topic getTopicInstance(String topicName, Session session) {
	    Topic topic = null;
	    try {
	      topic = session.createTopic(topicName);
	    } catch (JMSException e) {
	      logger.info("Exception: {}", e.getMessage());
	      return null;
	    }
	    return topic;
  }
}

