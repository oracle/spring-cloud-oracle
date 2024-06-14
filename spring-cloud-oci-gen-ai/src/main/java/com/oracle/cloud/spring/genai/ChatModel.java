/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

import java.util.List;

import com.oracle.bmc.generativeaiinference.model.BaseChatResponse;
import com.oracle.bmc.generativeaiinference.model.ChatChoice;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.CohereChatResponse;
import com.oracle.bmc.generativeaiinference.model.GenericChatResponse;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;

public interface ChatModel {
    ChatResponse chat(String prompt);
    default String extractText(ChatResponse chatResponse) {
        BaseChatResponse baseChatResponse = chatResponse.getChatResult().getChatResponse();
        if (baseChatResponse instanceof CohereChatResponse) {
            return ((CohereChatResponse) baseChatResponse).getText();
        } else if (baseChatResponse instanceof GenericChatResponse) {
            List<ChatChoice> choices =  ((GenericChatResponse) baseChatResponse).getChoices();
            List<ChatContent> contents = choices.get(choices.size() - 1).getMessage().getContent();
            ChatContent content = contents.get(contents.size() - 1);
            if (content instanceof TextContent) {
                return ((TextContent) content).getText();
            }
        }
        throw new IllegalStateException("Unexpected chat response type: " + baseChatResponse.getClass().getName());
    }
}
