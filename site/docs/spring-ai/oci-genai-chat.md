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

Use `spring.ai.model.chat=oci-genai` to select the provider. This is the default when no chat selector is configured; set `spring.ai.model.chat=none` to disable chat auto-configuration. For on-demand serving, configure an OCI compartment and model ID.

```yaml
spring:
  ai:
    model:
      chat: oci-genai
    oci:
      genai:
        authentication-type: FILE
        file: ~/.oci/config
        profile: DEFAULT
        chat:
          compartment: ocid1.compartment.oc1..example
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
          compartment: ocid1.compartment.oc1..example
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
| `spring.ai.model.chat` | Selects the Spring AI chat provider | No | `oci-genai` |
| `spring.ai.oci.genai.chat.compartment` | OCI compartment OCID for chat requests | Yes |  |
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
| `spring.ai.oci.genai.chat.stop` | Stop sequences | No |  |

## Authentication

OCI authentication is configured under `spring.ai.oci.genai.*`.

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.ai.oci.genai.authentication-type` | `FILE`, `INSTANCE_PRINCIPAL`, `RESOURCE_PRINCIPAL`, `WORKLOAD_IDENTITY`, `SIMPLE`, or `SESSION_TOKEN` | No | `FILE` |
| `spring.ai.oci.genai.federation-endpoint` | Federation endpoint for principal-based auth | No |  |
| `spring.ai.oci.genai.file` | OCI config file path | No |  |
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

The Spring AI Oracle chat provider supports synchronous text chat. Streaming, tool calling, structured output, multimodal chat, and rerank are planned as separate provider additions.
