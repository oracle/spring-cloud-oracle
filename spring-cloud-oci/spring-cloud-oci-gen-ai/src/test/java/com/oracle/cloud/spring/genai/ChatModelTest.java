/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.model.AssistantMessage;
import com.oracle.bmc.generativeaiinference.model.BaseChatResponse;
import com.oracle.bmc.generativeaiinference.model.ChatDetails;
import com.oracle.bmc.generativeaiinference.model.ChatChoice;
import com.oracle.bmc.generativeaiinference.model.ChatResult;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequest;
import com.oracle.bmc.generativeaiinference.model.CohereChatResponse;
import com.oracle.bmc.generativeaiinference.model.GenericChatResponse;
import com.oracle.bmc.generativeaiinference.model.Message;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.model.UserMessage;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @ParameterizedTest
    @MethodSource("requestShapeModels")
    void chatDoesNotReuseHistory(InferenceRequestType inferenceRequestType) throws Exception {
        try (GenerativeAiInference client = mock(GenerativeAiInference.class)) {
            when(client.chat(any())).thenReturn(mockChatResponse(CohereChatResponse.builder().text(chatText).build()));
            ChatModel chatModel = testChatModel(client, inferenceRequestType);
            chatModel.chat("first prompt");
            chatModel.chat("second prompt");

            ArgumentCaptor<ChatRequest> requestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
            verify(client, times(2)).chat(requestCaptor.capture());

            assertRequestContainsOnlyPrompt(requestCaptor.getAllValues().get(0).getChatDetails(), "first prompt");
            assertRequestContainsOnlyPrompt(requestCaptor.getAllValues().get(1).getChatDetails(), "second prompt");
        }
    }

    private ChatModel testChatModel(GenerativeAiInference client) {
        return testChatModel(client, InferenceRequestType.COHERE);
    }

    private ChatModel testChatModel(GenerativeAiInference client, InferenceRequestType inferenceRequestType) {
        return ChatModelImpl.builder()
                .client(client)
                .compartment("test")
                .servingMode(OnDemandServingMode.builder().modelId("").build())
                .inferenceRequestType(inferenceRequestType)
                .build();
    }

    private static Stream<Arguments> requestShapeModels() {
        return Stream.of(
                Arguments.of(InferenceRequestType.COHERE),
                Arguments.of(InferenceRequestType.LLAMA)
        );
    }

    private void assertRequestContainsOnlyPrompt(ChatDetails chatDetails, String prompt) {
        BaseChatRequest baseChatRequest = chatDetails.getChatRequest();
        if (baseChatRequest instanceof CohereChatRequest) {
            CohereChatRequest request = (CohereChatRequest) baseChatRequest;
            assertThat(request.getMessage()).isEqualTo(prompt);
            assertThat(request.getChatHistory()).isNull();
            return;
        }

        assertThat(baseChatRequest).isInstanceOf(com.oracle.bmc.generativeaiinference.model.GenericChatRequest.class);
        com.oracle.bmc.generativeaiinference.model.GenericChatRequest request =
                (com.oracle.bmc.generativeaiinference.model.GenericChatRequest) baseChatRequest;
        List<Message> messages = request.getMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).isInstanceOf(UserMessage.class);
        UserMessage userMessage = (UserMessage) messages.get(0);
        assertThat(userMessage.getContent()).hasSize(1);
        assertThat(userMessage.getContent().get(0)).isInstanceOf(TextContent.class);
        assertThat(((TextContent) userMessage.getContent().get(0)).getText()).isEqualTo(prompt);
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
