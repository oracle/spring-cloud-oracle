/*
 ** Copyright (c) 2025, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.database.spring.txeventq.producer.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.jms.core.JmsTemplate;

@Service
@SuppressFBWarnings(value = "EI2",
        justification = "JmsTemplate is an application-managed Spring dependency used to publish messages.")
public class Producer {

  private static final Logger log = LoggerFactory.getLogger(Producer.class);

  JmsTemplate jmsTemplate;

  @Value("${txeventq.topic.name}")
  private String topic;

  public Producer(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  public void sendMessageToTopic(String message)
  {
    jmsTemplate.convertAndSend(topic,message);
    log.info("Sending message: {} to topic {}", message, topic);
  }
}
