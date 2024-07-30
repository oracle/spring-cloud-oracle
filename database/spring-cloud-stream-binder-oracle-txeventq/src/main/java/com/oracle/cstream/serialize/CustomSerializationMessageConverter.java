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

package com.oracle.cstream.serialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

public class CustomSerializationMessageConverter extends SimpleMessageConverter {
	public String deserializer = null;
	
	private static final Logger logger = LoggerFactory.getLogger(CustomSerializationMessageConverter.class);

	public String getDeserializer() {
		return deserializer;
	}

	public void setDeserializer(String deserializer) {
		this.deserializer = deserializer;
	}
	
	@Override
	public Object fromMessage(Message jmsMessage) throws JMSException {
		Object result = super.fromMessage(jmsMessage);
		
		// get class object
		Class<?> deserializeClass = null;
		try {
			deserializeClass = Class.forName(deserializer);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Deserialization class not found: " + this.deserializer);
		}
		
		//verify that it is correct instance
		boolean isInstanceOfDeserializer = false;
    	for(Class<?> inter_face: deserializeClass.getInterfaces()) {
    		if(inter_face.toString().equals(Deserializer.class.toString())) {
    			isInstanceOfDeserializer = true;
    			break;
    		}
    	}
    	
    	if(!isInstanceOfDeserializer) {
    		logger.debug("The configured deserializer class is not an instance of 'com.oracle.cstream.serialize.DeSerializer'");
    		throw new IllegalArgumentException("The configured serializer class is not an instance of 'com.oracle.cstream.serialize.DeSerializer'");
    	}
    	
    	Deserializer<?> s = null;
    	
    	try {
    		s = (Deserializer<?>)(deserializeClass.getDeclaredConstructor().newInstance());
    	} catch(Exception e) {
    		logger.debug("Serializer object could not be initiated.");
    		throw new IllegalArgumentException("Serializer object could not be initiated.");
    	}
    	
    	result = (Object)(s.deserialize((byte[])result));
		
		return result;
	}
}
