/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

import java.util.Collections;
import java.util.stream.Stream;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.AssistantMessage;
import com.oracle.bmc.generativeaiinference.model.BaseChatResponse;
import com.oracle.bmc.generativeaiinference.model.ChatChoice;
import com.oracle.bmc.generativeaiinference.model.ChatResult;
import com.oracle.bmc.generativeaiinference.model.CohereChatResponse;
import com.oracle.bmc.generativeaiinference.model.GenericChatResponse;
import com.oracle.bmc.generativeaiinference.model.Message;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChatModelTest {
    static final String chatText = "Hello.";
    static final TextContent textContent = TextContent.builder().text(chatText).build();
    static final Message message = AssistantMessage.builder()
            .content(Collections.singletonList(textContent))
            .build();
    static final ChatChoice chatChoice = ChatChoice.builder()
            .message(message)
            .build();

    public static Stream<Arguments> chatResponses() {
        return Stream.of(
                Arguments.of(CohereChatResponse.builder()
                        .text(chatText)
                        .build()),
                Arguments.of(GenericChatResponse.builder()
                        .choices(Collections.singletonList(chatChoice))
                        .build())
        );
    }

    @ParameterizedTest
    @MethodSource("chatResponses")
    void chat(BaseChatResponse mockChat) throws Exception {
        try (GenerativeAiInference client = mock(GenerativeAiInference.class)) {
            ChatModel chatModel = testChatModel(client);
            when(client.chat(any())).thenReturn(mockChatResponse(mockChat));
            ChatResponse chatResponse = chatModel.chat("hello, world!");
            String chatTextActual = chatModel.extractText(chatResponse);
            assertThat(chatTextActual).isEqualTo(chatText);
        }
    }

    private ChatModel testChatModel(GenerativeAiInference client) {
        return ChatModelImpl.builder()
                .client(client)
                .compartment("test")
                .servingMode(OnDemandServingMode.builder().modelId("").build())
                .build();
    }

    private ChatResponse mockChatResponse(BaseChatResponse response) {
        ChatResult chatResult = ChatResult.builder()
                .chatResponse(response)
                .build();
        return ChatResponse.builder()
                .chatResult(chatResult)
                .build();
    }
}
