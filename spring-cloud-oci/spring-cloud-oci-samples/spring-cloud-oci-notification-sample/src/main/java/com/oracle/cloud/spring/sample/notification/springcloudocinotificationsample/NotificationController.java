/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.notification.springcloudocinotificationsample;

import com.oracle.bmc.ons.responses.CreateSubscriptionResponse;
import com.oracle.bmc.ons.responses.CreateTopicResponse;
import com.oracle.bmc.ons.responses.PublishMessageResponse;
import com.oracle.cloud.spring.notification.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demoapp/api/notifications")
public class NotificationController {

    @Autowired
    Notification notification;

    @PostMapping(value = "/topics/{topicId}/messages")
    ResponseEntity<?> publishMessage(@PathVariable String topicId,
                                     @RequestParam String title,
                                     @RequestParam String message) {
        PublishMessageResponse response = notification.publishMessage(topicId, title, message);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/subscriptions")
    ResponseEntity<?> createSubscription(@RequestParam String compartmentId,
                                         @RequestParam String topicId,
                                         @RequestParam (required = false) String protocol,
                                         @RequestParam String endpoint) {
        if (protocol == null) {
            protocol = "EMAIL";
        }
        CreateSubscriptionResponse response = notification.createSubscription(compartmentId, topicId, protocol, endpoint);
        String subscriptionId = response.getSubscription().getId();
        return ResponseEntity.accepted().body("subscription id : " + subscriptionId);
    }

    @GetMapping(value = "/subscriptions/{subscriptionId}")
    ResponseEntity<?> getNotificationSubscription(@PathVariable String subscriptionId) {
        String response = notification.getSubscription(subscriptionId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/subscriptions")
    ResponseEntity<?> listNotificationSubscriptions(@RequestParam String topicId,
                                                    @RequestParam String compartmentId) {
        String response = notification.listSubscriptions(topicId, compartmentId);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping(value = "/topics")
    ResponseEntity<?> createTopic(@RequestParam String topicName,
                                  @RequestParam String compartmentId) {
        CreateTopicResponse response = notification.createTopic(topicName, compartmentId);
        String topicId = response.getNotificationTopic().getTopicId();
        return ResponseEntity.accepted().body("topic id : " + topicId);
    }
}
