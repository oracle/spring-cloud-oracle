---
title: OCI Generative AI Chat
sidebar_position: 1
---

# OCI Generative AI Chat

Spring AI Oracle provides a Spring AI `ChatModel` backed by [OCI Generative AI](https://docs.oracle.com/en-us/iaas/Content/generative-ai/home.htm).

This is the replacement path for new Spring AI applications that want to call OCI Generative AI through standard Spring AI `ChatModel` and `ChatClient` APIs.

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.spring.ai</groupId>
  <artifactId>spring-ai-starter-model-oracle</artifactId>
</dependency>
```

## On-Demand Chat

Use `spring.ai.model.chat=oracle` to select the provider. For on-demand serving, configure an OCI compartment and model ID.

```yaml
spring:
  ai:
    model:
      chat: oracle
    oracle:
      auth:
        type: FILE
        config-file: ~/.oci/config
        profile: DEFAULT
      chat:
        options:
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
      chat: oracle
    oracle:
      chat:
        options:
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

## Configuration

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.ai.model.chat` | Selects the Spring AI chat provider | Yes |  |
| `spring.ai.oracle.chat.options.compartment-id` | OCI compartment OCID for chat requests | Yes |  |
| `spring.ai.oracle.chat.options.serving-mode` | `ON_DEMAND` or `DEDICATED` | No | `ON_DEMAND` |
| `spring.ai.oracle.chat.options.model` | On-demand model ID | For on-demand |  |
| `spring.ai.oracle.chat.options.endpoint-id` | Dedicated endpoint OCID | For dedicated |  |
| `spring.ai.oracle.chat.options.api-format` | OCI chat request format: `GENERIC`, `COHERE_V2`, or `COHERE` | No | inferred |
| `spring.ai.oracle.chat.options.temperature` | Sampling temperature | No |  |
| `spring.ai.oracle.chat.options.top-p` | Top P sampling value | No |  |
| `spring.ai.oracle.chat.options.top-k` | Top K sampling value | No |  |
| `spring.ai.oracle.chat.options.max-tokens` | Maximum output tokens | No |  |
| `spring.ai.oracle.chat.options.frequency-penalty` | Repetition penalty | No |  |
| `spring.ai.oracle.chat.options.presence-penalty` | Presence penalty | No |  |
| `spring.ai.oracle.chat.options.stop-sequences` | Stop sequences | No |  |

## Authentication

OCI authentication is configured under `spring.ai.oracle.auth.*`.

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.ai.oracle.auth.type` | `FILE`, `INSTANCE_PRINCIPAL`, `RESOURCE_PRINCIPAL`, `WORKLOAD_IDENTITY`, `SIMPLE`, or `SESSION_TOKEN` | No | `FILE` |
| `spring.ai.oracle.auth.federation-endpoint` | Federation endpoint for principal-based auth | No |  |
| `spring.ai.oracle.auth.config-file` | OCI config file path | No |  |
| `spring.ai.oracle.auth.profile` | OCI config profile | No | `DEFAULT` |
| `spring.ai.oracle.auth.tenant-id` | Tenancy OCID for simple auth | For simple auth |  |
| `spring.ai.oracle.auth.user-id` | User OCID for simple auth | For simple auth |  |
| `spring.ai.oracle.auth.fingerprint` | API key fingerprint for simple auth | For simple auth |  |
| `spring.ai.oracle.auth.private-key` | Private key content for simple auth | For simple auth |  |
| `spring.ai.oracle.auth.pass-phrase` | Private key pass phrase | No |  |
| `spring.ai.oracle.auth.region` | OCI region | No |  |

If the application provides its own OCI `BasicAuthenticationDetailsProvider` or `GenerativeAiInference` bean, auto-configuration uses that bean instead of creating one from properties.

## Current Scope

The first Spring AI Oracle provider supports synchronous text chat. Streaming, tool calling, structured output, multimodal chat, embeddings, and rerank are planned as separate provider additions.
