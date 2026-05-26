---
title: OCI Generative AI Embeddings
sidebar_position: 2
---

# OCI Generative AI Embeddings (In Development)

Spring AI Oracle provides a Spring AI `EmbeddingModel` backed by [OCI Generative AI](https://docs.oracle.com/en-us/iaas/Content/generative-ai/home.htm).

This is the replacement path for new Spring AI applications that want to call OCI Generative AI embedding models through standard Spring AI APIs.

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.spring.ai</groupId>
  <artifactId>spring-ai-starter-model-oracle</artifactId>
</dependency>
```

## On-Demand Embeddings

Use `spring.ai.model.embedding=oci-genai` to select the provider. This is the default when no embedding selector is configured; set `spring.ai.model.embedding=none` to disable embedding auto-configuration. For on-demand serving, configure an OCI compartment and model ID.

```yaml
spring:
  ai:
    model:
      embedding: oci-genai
    oci:
      genai:
        authentication-type: FILE
        config-file: ~/.oci/config
        profile: DEFAULT
        embedding:
          compartment-id: ocid1.compartment.oc1..example
          serving-mode: ON_DEMAND
          model: cohere.embed-english-v3.0
          truncate: END
```

The OCI model catalog lists current model IDs, regions, and supported serving modes: [Pretrained Foundational Models in Generative AI](https://docs.oracle.com/en-us/iaas/Content/generative-ai/models.htm).

## Dedicated Endpoint Embeddings

For dedicated serving, configure the endpoint OCID instead of an on-demand model ID.

```yaml
spring:
  ai:
    model:
      embedding: oci-genai
    oci:
      genai:
        embedding:
          compartment-id: ocid1.compartment.oc1..example
          serving-mode: DEDICATED
          endpoint-id: ocid1.generativeaiendpoint.oc1..example
```

## Usage

Inject the standard Spring AI embedding model.

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

Runtime options can override configured defaults for a single request.

```java
import java.util.List;

import com.oracle.spring.ai.oracle.OracleGenAiEmbeddingOptions;
import com.oracle.spring.ai.oracle.api.OracleGenAiEmbeddingTruncate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

class RuntimeEmbeddingService {

    private final EmbeddingModel embeddingModel;

    RuntimeEmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    EmbeddingResponse embedForSearch(List<String> texts) {
        OracleGenAiEmbeddingOptions options = OracleGenAiEmbeddingOptions.builder()
                .model("cohere.embed-english-v3.0")
                .dimensions(512)
                .truncate(OracleGenAiEmbeddingTruncate.END)
                .build();

        return embeddingModel.call(new EmbeddingRequest(texts, options));
    }
}
```

## Observability

Embedding calls emit Spring AI model observations when an `ObservationRegistry` is available. Observations use Spring AI's standard embedding model convention and report OCI Generative AI as `oci_genai`.

Direct `OracleGenAiEmbeddingModel` construction uses `OracleGenAiEmbeddingModel.builder()`, which matches the Spring AI provider convention for optional retry and observation configuration.

## Configuration

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.ai.model.embedding` | Selects the Spring AI embedding provider | No | `oci-genai` |
| `spring.ai.oci.genai.embedding.compartment-id` | OCI compartment OCID for embedding requests | Yes |  |
| `spring.ai.oci.genai.embedding.serving-mode` | `ON_DEMAND` or `DEDICATED` | No | `ON_DEMAND` |
| `spring.ai.oci.genai.embedding.model` | On-demand model ID | For on-demand |  |
| `spring.ai.oci.genai.embedding.endpoint-id` | Dedicated endpoint OCID | For dedicated |  |
| `spring.ai.oci.genai.embedding.dimensions` | Output embedding dimensions | No |  |
| `spring.ai.oci.genai.embedding.truncate` | Truncation mode: `NONE`, `START`, or `END` | No | `NONE` |

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

The first embedding provider supports synchronous text embeddings that return float vectors. Image embeddings and non-float embedding formats are planned as separate provider additions.
