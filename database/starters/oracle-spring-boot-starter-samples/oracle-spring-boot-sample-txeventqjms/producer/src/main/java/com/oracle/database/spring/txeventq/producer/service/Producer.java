package com.oracle.database.spring.txeventq.producer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.jms.core.JmsTemplate;

@Service
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
