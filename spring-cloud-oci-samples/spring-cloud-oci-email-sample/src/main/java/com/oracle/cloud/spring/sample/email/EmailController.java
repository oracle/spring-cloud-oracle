/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.email;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("demoapp/api/email")
@Tag(name = "Email Service APIs")
public class EmailController {

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailService emailService;

    @RequestMapping(
            value = { "/send" },
            method = RequestMethod.POST,
            produces = "application/json",
            consumes = "application/json")
    @ResponseBody
    public String sendPost(@RequestBody EmailService.MailContent content) throws Exception {
        emailService.sendSimpleMessage(content);
        return "Done";
    }



}
