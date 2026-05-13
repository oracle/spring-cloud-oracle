# Spring AI Oracle

Spring AI model bindings for Oracle Cloud Infrastructure Generative AI.

This subproject is intentionally separate from the existing Spring Cloud OCI Maven tree and uses `org.springframework.ai:spring-ai-parent:2.0.0-M6` as its parent.

The model bindings include Spring AI `ChatModel` and `EmbeddingModel` implementations backed by OCI Generative AI Inference.

## Modules

| Module | Description |
|--------|-------------|
| `models/spring-ai-oracle` | OCI Generative AI `ChatModel` and `EmbeddingModel` implementations. |
| `auto-configurations/models/spring-ai-autoconfigure-model-oracle` | Spring Boot auto-configuration for OCI Generative AI Spring AI models. |
| `starters/spring-ai-starter-model-oracle` | Spring Boot starter for OCI Generative AI Spring AI models. |

## Maven

```xml
<dependency>
  <groupId>com.oracle.spring.ai</groupId>
  <artifactId>spring-ai-starter-model-oracle</artifactId>
</dependency>
```

## Configuration

Oracle providers follow Spring AI's OCI GenAI model selectors. Chat and embedding default to `oci-genai`; set a selector to `none` to disable that model type.

```yaml
spring:
  ai:
    model:
      chat: oci-genai
      embedding: oci-genai
    oci:
      genai:
        authentication-type: FILE
        config-file: ~/.oci/config
        profile: DEFAULT
        chat:
          compartment-id: ocid1.compartment.oc1..example
          serving-mode: ON_DEMAND
          model: cohere.command-a-03-2025
        embedding:
          compartment-id: ocid1.compartment.oc1..example
          serving-mode: ON_DEMAND
          model: cohere.embed-english-v3.0
          truncate: END
```

Dedicated endpoints use `DEDICATED` serving mode and `endpoint-id`:

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

Supported authentication types:

- `FILE`
- `INSTANCE_PRINCIPAL`
- `RESOURCE_PRINCIPAL`
- `WORKLOAD_IDENTITY`
- `SIMPLE`
- `SESSION_TOKEN`

## Usage

Use the standard Spring AI `ChatModel` or `ChatClient` APIs:

```java
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
class GenAiService {

    private final ChatClient chatClient;

    GenAiService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    String ask(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
```

Spring AI Oracle follows Spring AI chat memory conventions. The `ChatModel` is stateless and consumes the message history supplied in each `Prompt`. For `ChatClient` use, configure Spring AI's `MessageChatMemoryAdvisor` with a `ChatMemory` bean and pass `ChatMemory.CONVERSATION_ID` on each conversational request:

```java
ChatClient chatClient = ChatClient.builder(chatModel)
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .build();

return chatClient.prompt()
        .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
        .user(prompt)
        .call()
        .content();
```

Spring AI Oracle supports Spring AI tool calling for OCI `GENERIC` and `COHERE_V2` chat API formats. Register tools with the standard Spring AI `ChatClient` APIs; legacy `COHERE` models continue to support text chat but reject tool definitions, assistant tool calls, and tool response messages.

```java
ChatClient chatClient = ChatClient.builder(chatModel)
        .defaultTools(weatherTools)
        .build();

return chatClient.prompt()
        .user("Do I need an umbrella in Seattle?")
        .call()
        .content();
```

Use the standard Spring AI `EmbeddingModel` API for text embeddings:

```java
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    float[] embed(String text) {
        return embeddingModel.embed(text);
    }
}
```

See the OCI Generative AI model list for current model IDs, regions, and supported serving modes:
<https://docs.oracle.com/en-us/iaas/Content/generative-ai/models.htm>.

The chat API format is inferred from the model ID unless `spring.ai.oci.genai.chat.api-format` is set explicitly:
Command A uses `COHERE_V2`, other Cohere chat models use `COHERE`, and non-Cohere model families use `GENERIC`.

## Live Tests

The live OCI Generative AI test uses config file authentication and is skipped unless a compartment OCID is provided. The test lists active chat-capable models from OCI, keeps the latest current text on-demand model from each provider, uses the same model ID API-format inference as production, and runs one dynamic test for each selected provider model. Set `OCI_GENAI_MODEL` to run only one specific current model from the live model list.

```bash
OCI_COMPARTMENT_ID=ocid1.compartment.oc1..example \
mvn -f spring-ai-oracle/pom.xml test
```

Optional config file authentication overrides use `OCI_CONFIG_FILE`, `OCI_PROFILE`, and `OCI_REGION`.

Set `OCI_GENAI_MODEL` to run one chat model and `OCI_GENAI_EMBEDDING_MODEL` to run one embedding model.

## License

Copyright (c) 2026, Oracle and/or its affiliates.

Released under the Universal Permissive License v1.0 as shown at
<https://oss.oracle.com/licenses/upl/>.
