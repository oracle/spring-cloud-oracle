---
title: OCI Generative AI Chat
sidebar_position: 1
---

# OCI Generative AI Chat (In Development)

Spring AI Oracle provides a Spring AI `ChatModel` backed by [OCI Generative AI](https://docs.oracle.com/en-us/iaas/Content/generative-ai/home.htm).

This is the replacement path for new Spring AI applications that want to call OCI Generative AI through standard Spring AI `ChatModel` and `ChatClient` APIs.

Spring AI Oracle is built against Spring AI `2.0.0-RC1`.

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.spring.ai</groupId>
  <artifactId>spring-ai-starter-model-oracle</artifactId>
</dependency>
```

## On-Demand Chat

Use `spring.ai.model.chat=oci-genai` to select the provider. This is the default when no chat selector is configured; set `spring.ai.model.chat=none` to disable chat auto-configuration. For on-demand serving, configure an OCI compartment and model ID.

```yaml
spring:
  ai:
    model:
      chat: oci-genai
    oci:
      genai:
        authentication-type: FILE
        config-file: ~/.oci/config
        profile: DEFAULT
        chat:
          compartment-id: ocid1.compartment.oc1..example
          serving-mode: ON_DEMAND
          model: cohere.command-a-03-2025
          temperature: 0.2
          max-tokens: 512
```

The OCI model catalog lists current model IDs, regions, API formats, and supported serving modes: [Pretrained Foundational Models in Generative AI](https://docs.oracle.com/en-us/iaas/Content/generative-ai/models.htm).

## Dedicated Endpoint Chat

For dedicated serving, configure the endpoint OCID instead of an on-demand model ID.

```yaml
spring:
  ai:
    model:
      chat: oci-genai
    oci:
      genai:
        chat:
          compartment-id: ocid1.compartment.oc1..example
          serving-mode: DEDICATED
          endpoint-id: ocid1.generativeaiendpoint.oc1..example
```

## Usage

Inject the standard Spring AI model or build a `ChatClient` from it.

```java
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
class AnswerService {

    private final ChatClient chatClient;

    AnswerService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    String answer(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
```

Use the standard Spring AI streaming APIs when the application should receive incremental chat responses.

```java
import reactor.core.publisher.Flux;

Flux<String> answerStream(String question) {
    return chatClient.prompt()
            .user(question)
            .stream()
            .content();
}
```

Direct `ChatModel` callers can use `stream(Prompt)` and consume the returned `Flux<ChatResponse>`. Streaming uses OCI Generative AI server-sent events and supports the same text chat request formats as synchronous chat: `GENERIC`, `COHERE_V2`, and legacy `COHERE`.

## Observability

Chat calls emit Spring AI model observations when an `ObservationRegistry` is available. Observations use Spring AI's standard chat model convention, include synchronous and streaming requests, and report OCI Generative AI as `oci_genai`.

Direct `OracleGenAiChatModel` construction uses `OracleGenAiChatModel.builder()`, which matches the Spring AI provider convention for optional dependencies such as retry, tool calling, and observation configuration.

## Conversation Memory

Spring AI Oracle follows Spring AI chat memory conventions. The chat model is stateless and does not store conversation history; `MessageChatMemoryAdvisor` loads prior turns from `ChatMemory` and adds them to the prompt as typed Spring AI messages before the request reaches OCI Generative AI.

The starter includes Spring AI chat memory auto-configuration. Applications can use the default in-memory implementation for simple cases or provide their own `ChatMemory` or `ChatMemoryRepository` bean.

```java
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;

class ConversationalAnswers {

    private final ChatClient chatClient;

    ConversationalAnswers(ChatModel chatModel, ChatMemory chatMemory) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    String answer(String conversationId, String question) {
        return chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(question)
                .call()
                .content();
    }
}
```

Direct `ChatModel` callers manage memory explicitly by retrieving messages from `ChatMemory` and passing them in a `Prompt`.

## Tool Calling

Spring AI Oracle supports Spring AI tool calling for OCI `GENERIC` and `COHERE_V2` chat API formats. Register tools with the standard Spring AI `ChatClient` APIs; the model sends tool definitions to OCI Generative AI, executes returned tool calls through Spring AI's `ToolCallingManager`, and returns the final model response.

Synchronous and streaming calls use the same internal tool execution semantics. When a streaming response contains tool calls and internal tool execution is enabled, the provider buffers the tool-call stream and emits only the direct tool result or the final model response after tool execution.

```java
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;

class WeatherTools {

    @Tool(description = "Get the current weather for a city")
    String weather(String city) {
        return "72F and clear in " + city;
    }
}

class ToolCallingAnswers {

    private final ChatClient chatClient;

    ToolCallingAnswers(ChatClient.Builder chatClientBuilder, WeatherTools weatherTools) {
        this.chatClient = chatClientBuilder
                .defaultTools(weatherTools)
                .build();
    }

    String answer(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
```

Legacy `COHERE` chat requests continue to support text chat, but tool calling requires `GENERIC` or `COHERE_V2`. Use a `COHERE_V2` model such as Cohere Command A, or set `spring.ai.oci.genai.chat.api-format=GENERIC` for model families that use the generic OCI chat request shape.

The live OCI Generative AI chat test suite includes bounded tool-calling coverage for selected `GENERIC` and `COHERE_V2` chat models when `OCI_COMPARTMENT_ID` is set. Legacy `COHERE` models remain covered by text chat only because OCI does not support tool definitions or tool response messages for that chat format.

Applications can customize tool execution eligibility by providing a `ToolExecutionEligibilityChecker` bean. Auto-configuration uses that bean when constructing the `OracleGenAiChatModel`; otherwise it treats chat responses with tool calls as eligible for Spring AI's internal tool execution.

## Configuration

Chat option properties are Spring Boot bindable and can also be supplied per request through Spring AI `Prompt` options. Runtime `Prompt` options override the configured defaults when both are present.

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.ai.model.chat` | Selects the Spring AI chat provider | No | `oci-genai` |
| `spring.ai.oci.genai.chat.compartment-id` | OCI compartment OCID for chat requests | Yes |  |
| `spring.ai.oci.genai.chat.serving-mode` | `ON_DEMAND` or `DEDICATED` | No | `ON_DEMAND` |
| `spring.ai.oci.genai.chat.model` | On-demand model ID | For on-demand |  |
| `spring.ai.oci.genai.chat.endpoint-id` | Dedicated endpoint OCID | For dedicated |  |
| `spring.ai.oci.genai.chat.api-format` | OCI chat request format: `GENERIC`, `COHERE_V2`, or `COHERE`; inferred as `COHERE_V2` for Cohere Command A, `COHERE` for other Cohere chat models, and `GENERIC` for other model families | No | inferred |
| `spring.ai.oci.genai.chat.temperature` | Sampling temperature | No |  |
| `spring.ai.oci.genai.chat.top-p` | Top P sampling value | No |  |
| `spring.ai.oci.genai.chat.top-k` | Top K sampling value | No |  |
| `spring.ai.oci.genai.chat.max-tokens` | Maximum output tokens | No |  |
| `spring.ai.oci.genai.chat.frequency-penalty` | Repetition penalty | No |  |
| `spring.ai.oci.genai.chat.presence-penalty` | Presence penalty | No |  |
| `spring.ai.oci.genai.chat.stop-sequences` | Stop sequences | No |  |

## Authentication

OCI authentication is configured under `spring.ai.oci.genai.*`.

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.ai.oci.genai.authentication-type` | `FILE`, `INSTANCE_PRINCIPAL`, `RESOURCE_PRINCIPAL`, `WORKLOAD_IDENTITY`, `SIMPLE`, or `SESSION_TOKEN` | No | `FILE` |
| `spring.ai.oci.genai.federation-endpoint` | Federation endpoint for principal-based auth | No |  |
| `spring.ai.oci.genai.config-file` | OCI config file path | No |  |
| `spring.ai.oci.genai.profile` | OCI config profile | No | `DEFAULT` |
| `spring.ai.oci.genai.tenant-id` | Tenancy OCID for simple auth | For simple auth |  |
| `spring.ai.oci.genai.user-id` | User OCID for simple auth | For simple auth |  |
| `spring.ai.oci.genai.fingerprint` | API key fingerprint for simple auth | For simple auth |  |
| `spring.ai.oci.genai.private-key` | Private key content for simple auth | For simple auth |  |
| `spring.ai.oci.genai.pass-phrase` | Private key pass phrase | No |  |
| `spring.ai.oci.genai.region` | OCI region | No |  |
| `spring.ai.oci.genai.endpoint` | OCI Generative AI inference endpoint override | No |  |

If the application provides its own OCI `BasicAuthenticationDetailsProvider` or `GenerativeAiInference` bean, auto-configuration uses that bean instead of creating one from properties.

## Current Scope

The Spring AI Oracle chat provider supports synchronous and streaming text chat. Tool calling is supported for `GENERIC` and `COHERE_V2` chat formats. Structured output, multimodal chat, and rerank are planned as separate provider additions.
