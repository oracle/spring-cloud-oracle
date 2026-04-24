/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.genai.springcloudocigenaisample;

import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import com.oracle.cloud.spring.genai.ChatModel;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatIT {
    public void chat(ChatModel chatModel) {
        ChatResponse r1 = chatModel.chat("Show me an example of a JUnit 5 test named sampleTest.");
        String text1 = chatModel.extractText(r1);
        assertThat(text1).contains("@Test").contains("sampleTest");
        ChatResponse r2 = chatModel.chat("Explain what the @Test annotation does in JUnit 5.");
        String text2 = chatModel.extractText(r2);
        assertThat(text2).containsIgnoringCase("test");
    }
}
