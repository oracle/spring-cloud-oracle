package com.oracle.database.spring.txeventqjms.consumer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

  private static final Logger log = LoggerFactory.getLogger(Consumer.class);

  @JmsListener(destination = "${txeventq.topic.name}")
  public void receiveMessage(String message) {
    log.info("Received message: {}", message);
  }
}
