/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.util.List;

import com.oracle.bmc.Region;
import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequest;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequestV2;
import com.oracle.bmc.generativeaiinference.model.GenericChatRequest;
import com.oracle.bmc.generativeaiinference.requests.ApplyGuardrailsRequest;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.requests.EmbedTextRequest;
import com.oracle.bmc.generativeaiinference.requests.GenerateTextRequest;
import com.oracle.bmc.generativeaiinference.requests.RerankTextRequest;
import com.oracle.bmc.generativeaiinference.requests.SummarizeTextRequest;
import com.oracle.bmc.generativeaiinference.responses.ApplyGuardrailsResponse;
import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;
import com.oracle.bmc.generativeaiinference.responses.GenerateTextResponse;
import com.oracle.bmc.generativeaiinference.responses.RerankTextResponse;
import com.oracle.bmc.generativeaiinference.responses.SummarizeTextResponse;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OracleGenAiChatModelTests {

    private static final GenerativeAiInference CLIENT = new NoOpGenerativeAiInference();

    @Test
    void createsGenericRequest() {
        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, defaultOptions())
                .toBaseChatRequest(new Prompt(List.of(
                        new SystemMessage("answer briefly"),
                        new UserMessage("hello"))), defaultOptions());

        assertThat(request).isInstanceOf(GenericChatRequest.class);
        GenericChatRequest genericRequest = (GenericChatRequest) request;
        assertThat(genericRequest.getMessages()).hasSize(2);
        assertThat(genericRequest.getMaxTokens()).isEqualTo(20);
        assertThat(genericRequest.getTemperature()).isEqualTo(0.2);
    }

    @Test
    void createsCohereV2Request() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(OracleGenAiChatOptions.ApiFormat.COHERE_V2);

        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, options)
                .toBaseChatRequest(new Prompt(List.of(new SystemMessage("rules"), new UserMessage("hello"))), options);

        assertThat(request).isInstanceOf(CohereChatRequestV2.class);
        assertThat(((CohereChatRequestV2) request).getMessages()).hasSize(2);
    }

    @Test
    void createsLegacyCohereRequest() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(OracleGenAiChatOptions.ApiFormat.COHERE);

        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, options)
                .toBaseChatRequest(new Prompt(List.of(
                        new SystemMessage("rules"),
                        new UserMessage("first"),
                        new AssistantMessage("second"),
                        new UserMessage("third"))), options);

        assertThat(request).isInstanceOf(CohereChatRequest.class);
        CohereChatRequest cohereRequest = (CohereChatRequest) request;
        assertThat(cohereRequest.getMessage()).isEqualTo("third");
        assertThat(cohereRequest.getPreambleOverride()).isEqualTo("rules");
        assertThat(cohereRequest.getChatHistory()).hasSize(2);
    }

    @Test
    void validatesOnDemandModelId() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setModel(null);
        OracleGenAiChatModel model = new OracleGenAiChatModel(CLIENT, options);

        assertThatThrownBy(() -> model.toChatRequest(new Prompt("hello"), options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("options.model");
    }

    @Test
    void validatesDedicatedEndpointId() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setServingMode(OracleGenAiChatOptions.ServingMode.DEDICATED);
        options.setEndpointId(null);
        OracleGenAiChatModel model = new OracleGenAiChatModel(CLIENT, options);

        assertThatThrownBy(() -> model.toChatRequest(new Prompt("hello"), options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("options.endpointId");
    }

    @Test
    void createsDedicatedServingMode() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setServingMode(OracleGenAiChatOptions.ServingMode.DEDICATED);
        options.setEndpointId("endpoint");

        ChatRequest request = new OracleGenAiChatModel(CLIENT, options)
                .toChatRequest(new Prompt("hello"), options);

        assertThat(request.getChatDetails().getServingMode())
                .isInstanceOf(com.oracle.bmc.generativeaiinference.model.DedicatedServingMode.class);
    }

    @Test
    void rejectsToolResponseMessages() {
        OracleGenAiChatModel model = new OracleGenAiChatModel(CLIENT, defaultOptions());
        ToolResponseMessage toolResponseMessage = ToolResponseMessage.builder()
                .responses(List.of(new ToolResponseMessage.ToolResponse("id", "tool", "{}")))
                .build();

        assertThatThrownBy(() -> model.toBaseChatRequest(new Prompt(toolResponseMessage), defaultOptions()))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("tool response");
    }

    @Test
    void rejectsAssistantToolCalls() {
        OracleGenAiChatModel model = new OracleGenAiChatModel(CLIENT, defaultOptions());
        AssistantMessage assistantMessage = AssistantMessage.builder()
                .content("calling tool")
                .toolCalls(List.of(new AssistantMessage.ToolCall("id", "function", "tool", "{}")))
                .build();

        assertThatThrownBy(() -> model.toBaseChatRequest(new Prompt(assistantMessage), defaultOptions()))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("tool calls");
    }

    private static OracleGenAiChatOptions defaultOptions() {
        return OracleGenAiChatOptions.builder()
                .compartmentId("compartment")
                .model("model")
                .temperature(0.2)
                .maxTokens(20)
                .build();
    }

    private static final class NoOpGenerativeAiInference implements GenerativeAiInference {

        @Override
        public void refreshClient() {
        }

        @Override
        public void setEndpoint(String endpoint) {
        }

        @Override
        public String getEndpoint() {
            return null;
        }

        @Override
        public void setRegion(Region region) {
        }

        @Override
        public void setRegion(String regionId) {
        }

        @Override
        public void useRealmSpecificEndpointTemplate(boolean realmSpecificEndpointTemplateEnabled) {
        }

        @Override
        public ApplyGuardrailsResponse applyGuardrails(ApplyGuardrailsRequest request) {
            throw new UnsupportedOperationException("No OCI calls are expected in request mapping tests.");
        }

        @Override
        public com.oracle.bmc.generativeaiinference.responses.ChatResponse chat(ChatRequest request) {
            throw new UnsupportedOperationException("No OCI calls are expected in request mapping tests.");
        }

        @Override
        public EmbedTextResponse embedText(EmbedTextRequest request) {
            throw new UnsupportedOperationException("No OCI calls are expected in request mapping tests.");
        }

        @Override
        public GenerateTextResponse generateText(GenerateTextRequest request) {
            throw new UnsupportedOperationException("No OCI calls are expected in request mapping tests.");
        }

        @Override
        public RerankTextResponse rerankText(RerankTextRequest request) {
            throw new UnsupportedOperationException("No OCI calls are expected in request mapping tests.");
        }

        @Override
        public SummarizeTextResponse summarizeText(SummarizeTextRequest request) {
            throw new UnsupportedOperationException("No OCI calls are expected in request mapping tests.");
        }

        @Override
        public void close() {
        }
    }
}
