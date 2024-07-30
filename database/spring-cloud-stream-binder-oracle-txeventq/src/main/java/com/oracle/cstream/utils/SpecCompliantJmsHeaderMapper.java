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

package com.oracle.cstream.utils;

import jakarta.jms.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.jms.DefaultJmsHeaderMapper;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.SerializationUtils;

public class SpecCompliantJmsHeaderMapper extends DefaultJmsHeaderMapper {

  private static final Logger logger = LoggerFactory.getLogger(
    SpecCompliantJmsHeaderMapper.class
  );
  
  private static final List<Class<?>> SUPPORTED_PROPERTY_TYPES =
			Arrays.asList(Boolean.class, Byte.class, Double.class, Float.class, Integer.class, Long.class, Short.class,
					String.class, byte[].class, UUID.class);
  
  private static final List<String> DEFAULT_TO_STRING_CLASSES =
			Arrays.asList(
					"org.springframework.util.MimeType",
					"org.springframework.http.MediaType"
			);
  
  public void addDefaultToStringClass(String className) {
	  SpecCompliantJmsHeaderMapper.DEFAULT_TO_STRING_CLASSES.add(className);
  }
  
  public List<String> getDefaultToStringClasses() {
	  return new ArrayList<>(SpecCompliantJmsHeaderMapper.DEFAULT_TO_STRING_CLASSES);
  }

  @Override
  public void fromHeaders(MessageHeaders headers, Message jmsMessage) {
    Map<String, Object> compliantHeaders = new HashMap<>(headers.size());
    for (Map.Entry<String, Object> entry : headers.entrySet()) {
      if (entry.getKey().contains("-")) {
        String key = entry.getKey().replaceAll("-", "_");
        logger.trace("Rewriting header name '{}' to conform to JMS spec", key);
        compliantHeaders.put(key, entry.getValue());
      } else {
        compliantHeaders.put(entry.getKey(), entry.getValue());
      }
    }
    
    // for each header, if its value belongs to toString
    // classes, convert to String
    for (Map.Entry<String, Object> entry : compliantHeaders.entrySet()){
        Object value = entry.getValue();
        if(SpecCompliantJmsHeaderMapper.DEFAULT_TO_STRING_CLASSES.contains(value.getClass().getName())) {
        	compliantHeaders.put(entry.getKey(), value.toString());
        } else if(!SUPPORTED_PROPERTY_TYPES.contains(value.getClass())) {
        	if(value instanceof Serializable) {
        		logger.info("Serializing {} header object", value);
        		compliantHeaders.put(entry.getKey(), SerializationUtils.serialize(value));
        	} else {
        		logger.info("Storing String representation for header: {}", entry.getKey());
        		compliantHeaders.put(entry.getKey(), value.toString());
        	}
        }
    }

    super.fromHeaders(new MessageHeaders(compliantHeaders), jmsMessage);
  }
}
