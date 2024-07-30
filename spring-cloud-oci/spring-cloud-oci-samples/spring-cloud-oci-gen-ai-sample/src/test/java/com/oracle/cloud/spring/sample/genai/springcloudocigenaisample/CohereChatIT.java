/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.genai.springcloudocigenaisample;

import com.oracle.cloud.spring.genai.ChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OCI_COMPARTMENT_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "OCI_COHERE_MODEL_ID", matches = ".+")
@ActiveProfiles("chat-cohere")
public class CohereChatIT extends ChatIT {
    @Autowired
    ChatModel chatModel;

    @Test
    public void chat() {
        super.chat(chatModel);
    }
}
