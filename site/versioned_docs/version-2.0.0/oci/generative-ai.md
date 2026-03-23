---
title: Generative AI
sidebar_position: 13
---

# Generative AI

[OCI Generative AI](https://docs.oracle.com/en-us/iaas/Content/generative-ai/home.htm) provides managed chat and embedding models through Spring-friendly abstractions.

## Dependency Coordinates

### Maven

```xml
<dependency>
  <groupId>com.oracle.cloud.spring</groupId>
  <artifactId>spring-cloud-oci-starter-gen-ai</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("com.oracle.cloud.spring:spring-cloud-oci-starter-gen-ai")
}
```

## Using Chat Models

The starter auto-configures a `ChatModel` bean:

```java
@Autowired
private ChatModel chatModel;

public void chat() {
    ChatResponse response = chatModel.chat("my chat prompt");
}
```

## Using Embedding Models

The starter auto-configures an `EmbeddingModel` bean:

```java
@Autowired
private EmbeddingModel embeddingModel;

public void embed() {
    EmbedTextResponse response = embeddingModel.embed("my embedding text");
}
```

## Configuration

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.cloud.oci.genai.enabled` | Enables the OCI Generative AI client | No | `true` |
| `spring.cloud.oci.genai.embedding.enabled` | Enables embedding APIs | No | `false` |
| `spring.cloud.oci.genai.embedding.on-demand-model-id` | On-demand embedding model ID | No |  |
| `spring.cloud.oci.genai.embedding.dedicated-cluster-endpoint` | Dedicated embedding endpoint | No |  |
| `spring.cloud.oci.genai.embedding.compartment` | Compartment for embedding | Yes |  |
| `spring.cloud.oci.genai.embedding.truncate` | Truncation mode: `START`, `END`, or `NONE` | No | `NONE` |
| `spring.cloud.oci.genai.chat.enabled` | Enables chat APIs | No | `false` |
| `spring.cloud.oci.genai.chat.on-demand-model-id` | On-demand chat model ID | No |  |
| `spring.cloud.oci.genai.chat.dedicated-cluster-endpoint` | Dedicated chat endpoint | No |  |
| `spring.cloud.oci.genai.chat.compartment` | Compartment for chat | Yes |  |
| `spring.cloud.oci.genai.chat.preample-override` | Overrides the model preamble | No |  |
| `spring.cloud.oci.genai.chat.temperature` | Output temperature | No | `1.0` |
| `spring.cloud.oci.genai.chat.top-p` | Top P sampling value | No | `0.75` |
| `spring.cloud.oci.genai.chat.top-k` | Top K sampling value | No | `0.0` |
| `spring.cloud.oci.genai.chat.frequency-penalty` | Penalty for repeated tokens | No | `0.0` |
| `spring.cloud.oci.genai.chat.presence-penalty` | Penalty when a token already appears | No | `0.0` |
| `spring.cloud.oci.genai.chat.max-tokens` | Maximum output tokens | No | `600` |

For chat and embedding, specify either an on-demand model ID or a dedicated cluster endpoint for the respective feature.

## Sample

See [`spring-cloud-oci-gen-ai-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-gen-ai-sample).
