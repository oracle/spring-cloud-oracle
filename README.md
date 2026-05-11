# Spring Cloud Oracle

Spring Cloud Oracle brings Oracle AI Database, Oracle AI Database-native messaging, and Oracle Cloud Infrastructure (OCI) into the Spring application model. It provides Spring Boot starters, auto-configuration, templates, integrations, and sample applications for teams building data-intensive and cloud-connected services with Oracle technologies.

## Current Releases

| Project | Current release | What it provides |
| --- | --- | --- |
| [Spring Cloud OCI](./spring-cloud-oci/README.md) | v2.0.1 | Spring Boot integrations for OCI services such as Vault, Object Storage, Streaming, Functions, Queues, Notifications, Email Delivery, Autonomous Database, and Generative AI |
| [Oracle AI Database Spring Boot Starters](./database/starters/README.md) | v26.1.1 | Starters and auto-configuration for Oracle AI Database connectivity, UCP, Wallet, AQ/JMS, OKafka, JSON, spatial, and OpenTelemetry workloads |
| [Spring Cloud Stream Binder for Oracle TxEventQ](./database/spring-cloud-stream-binder-oracle-txeventq/README.md) | v0.18.0 | A Spring Cloud Stream binder for Oracle AI Database Transactional Event Queues |

[Spring AI Oracle](./spring-ai-oracle/README.md) is in development and is not released yet.

## Why Spring Cloud Oracle?

Spring teams using Oracle technologies often need more than raw client libraries. They need integrations that fit Spring Boot configuration, auto-configuration, resource handling, observability, and messaging conventions.

Spring Cloud Oracle focuses on that integration layer:

- Oracle AI Database starters for connection pooling, Wallet support, AQ/JMS, OKafka, JSON Collections, JSON Relational Duality Views, spatial data, and OpenTelemetry
- A Spring Cloud Stream binder for Oracle AI Database Transactional Event Queues (TxEventQ)
- OCI integrations exposed through Spring-friendly APIs such as property sources, resources, templates, and auto-configured clients
- Sample applications that show dependencies, configuration, and usage patterns in context

## Compatibility

- The 1.x release line tracks Spring Boot 3.
- The 2.x release line tracks Spring Boot 4 and Spring Framework 7.

## Documentation

Start with the current Docusaurus documentation:

- [Spring Cloud Oracle reference documentation](https://oracle.github.io/spring-cloud-oracle/site/docs/intro)
- [Changelog](./site/docs/releases/changelog.md)

Current API documentation:

| Release                   | Reference Docs                                                                 | OCI API Docs                                                                           | Database API Docs                                                                              |
|---------------------------|--------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| Spring Cloud Oracle 2.0.1 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/site/docs/intro) | [OCI API Docs](https://oracle.github.io/spring-cloud-oracle/2.0.1/javadocs/index.html) | [Database API Docs](https://oracle.github.io/spring-cloud-oracle/2.0.1/javadocs-db/index.html) |
| Spring Cloud Oracle 2.0.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/site/docs/intro) | [OCI API Docs](https://oracle.github.io/spring-cloud-oracle/2.0.0/javadocs/index.html) | [Database API Docs](https://oracle.github.io/spring-cloud-oracle/2.0.0/javadocs-db/index.html) |

<details>
<summary>Archived 1.x documentation</summary>

| Release                   | Reference Docs                                                                                 | API Docs                                                                               | Database API Docs                                                                              |
|---------------------------|------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| Spring Cloud Oracle 1.4.5 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.4.5/reference/html/index.html) | [OCI API Docs](https://oracle.github.io/spring-cloud-oracle/1.4.5/javadocs/index.html) | [Database API Docs](https://oracle.github.io/spring-cloud-oracle/1.4.5/javadocs-db/index.html) |
| Spring Cloud Oracle 1.4.4 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.4.4/reference/html/index.html) | [OCI API Docs](https://oracle.github.io/spring-cloud-oracle/1.4.4/javadocs/index.html) | [Database API Docs](https://oracle.github.io/spring-cloud-oracle/1.4.4/javadocs-db/index.html) |
| Spring Cloud Oracle 1.4.3 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.4.3/reference/html/index.html) | [OCI API Docs](https://oracle.github.io/spring-cloud-oracle/1.4.3/javadocs/index.html) | [Database API Docs](https://oracle.github.io/spring-cloud-oracle/1.4.3/javadocs-db/index.html) |
| Spring Cloud Oracle 1.4.2 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.4.2/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.4.2/javadocs/index.html)     |                                                                                                |
| Spring Cloud Oracle 1.4.1 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.4.1/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.4.1/javadocs/index.html)     |                                                                                                |
| Spring Cloud Oracle 1.4.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.4.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.4.0/javadocs/index.html)     |                                                                                                |
| Spring Cloud Oracle 1.3.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.3.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.3.0/javadocs/index.html)     |                                                                                                |
| Spring Cloud Oracle 1.2.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.2.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.2.0/javadocs/index.html)     |                                                                                                |
| Spring Cloud Oracle 1.1.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.1.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.1.0/javadocs/index.html)     |                                                                                                |
| Spring Cloud Oracle 1.0.1 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.0.1/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.0.1/javadocs/index.html)     |                                                                                                |
| Spring Cloud Oracle 1.0.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.0.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.0.0/javadocs/index.html)     |                                                                                                |

</details>

## Samples

The repository includes sample applications for each major project area:

- [Spring Cloud OCI samples](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples)
- [Oracle AI Database starter samples](https://github.com/oracle/spring-cloud-oracle/tree/main/database/starters/oracle-spring-boot-starter-samples)
- [TxEventQ binder sample](https://github.com/oracle/spring-cloud-oracle/tree/main/database/spring-cloud-stream-binder-oracle-txeventq/spring-cloud-stream-binder-txeventq-sample)

## Roadmap

Planned and in-progress work includes:

- OpenRewrite recipes to help migrate from HikariCP to Oracle Universal Connection Pool
- Further simplification for running Spring Boot applications as OCI Functions, including native images with GraalVM Native Image
- More idiomatic OCI service modules and broader use of Spring template patterns
- Additional Spring CLI project templates and sample applications, especially around Spring AI, retrieval augmented generation, and agentic workflows
- Deeper Oracle AI Database integration for Spring applications, including Spring Kafka support for Transactional Event Queues

Please open an issue in this repository to share feedback, report bugs, or suggest additional integrations.

## Related Resources

If you are building Spring Boot applications with Oracle AI Database, [Oracle Backend Microservices and AI](https://bit.ly/OracleAI-microservices) provides patterns for building, testing, and operating microservices platforms on any cloud or in your own infrastructure.

[CloudBank AI](https://bit.ly/cloudbankAI) is a free self-paced hands-on lab that covers Spring Boot microservices, REST APIs, asynchronous services with JMS, Oracle AI Database-backed persistence, service discovery, Spring Actuator, Prometheus, Grafana, Loki, OpenTelemetry, Jaeger, long-running actions, APISIX API Gateway, and an AI chatbot built with Ollama.

The [Oracle AI Microservices Sandbox](https://oracle-samples.github.io/oaim-sandbox/) is a developer-preview environment for exploring generative AI and retrieval augmented generation with Oracle AI Database vector search. It can run on a developer desktop, on-premises, in a container, or in Kubernetes.

## Contributing

This project welcomes contributions from the community. Before submitting a pull request, please [review the contribution guide](./CONTRIBUTING.md).

## Security

Please consult the [security guide](./SECURITY.md) for the responsible security vulnerability disclosure process.

## License

Copyright (c) 2023, 2026, Oracle and/or its affiliates.

Released under the Universal Permissive License v1.0 as shown at
<https://oss.oracle.com/licenses/upl/>.
