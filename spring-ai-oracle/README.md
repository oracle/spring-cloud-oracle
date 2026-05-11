# Spring AI Oracle

Spring AI model bindings for Oracle Cloud Infrastructure Generative AI.

This subproject is intentionally separate from the existing Spring Cloud OCI Maven tree and uses `org.springframework.ai:spring-ai-parent:2.0.0-M6` as its parent.

The first model binding is a chat-only Spring AI `ChatModel` backed by OCI Generative AI Inference.

## Modules

| Module | Description |
|--------|-------------|
| `models/spring-ai-oracle` | OCI Generative AI `ChatModel` implementation. |
| `auto-configurations/models/spring-ai-autoconfigure-model-oracle` | Spring Boot auto-configuration for the OCI Generative AI chat model. |
| `starters/spring-ai-starter-model-oracle` | Spring Boot starter for the OCI Generative AI chat model. |

## Maven

```xml
<dependency>
  <groupId>com.oracle.spring.ai</groupId>
  <artifactId>spring-ai-starter-model-oracle</artifactId>
</dependency>
```

## Configuration

Select the Oracle chat model provider with Spring AI's model selector:

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
```

Dedicated endpoints use `DEDICATED` serving mode and `endpoint-id`:

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

See the OCI Generative AI model list for current model IDs, regions, and supported serving modes:
<https://docs.oracle.com/en-us/iaas/Content/generative-ai/models.htm>.

## Live Tests

The live OCI Generative AI test uses config file authentication and is skipped unless a compartment OCID is provided. The test lists active chat-capable models from OCI, keeps the latest current text on-demand model from each provider, maps model IDs to OCI chat API formats, and runs one dynamic test for each selected provider model. Set `OCI_GENAI_MODEL` to run only one specific current model from the live model list.

```bash
OCI_COMPARTMENT_ID=ocid1.compartment.oc1..example \
mvn -f spring-ai-oracle/pom.xml test
```

Optional config file authentication overrides use `OCI_CONFIG_FILE`, `OCI_PROFILE`, and `OCI_REGION`.

## License

Copyright (c) 2026, Oracle and/or its affiliates.

Released under the Universal Permissive License v1.0 as shown at
<https://oss.oracle.com/licenses/upl/>.
