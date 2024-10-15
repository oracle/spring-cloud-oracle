# Spring Cloud Oracle

Many Oracle customers make extensive use of Spring in their environments. They use Oracle Database and Oracle Cloud Infrastructure (OCI) services. Oracle Database is available in OCI, Azure, GCP and AWS, making it truly a multi-cloud database, and on-premises including in containers and Kubernetes. Oracle is trusted by many of the world’s top businesses, governments and other organizations to protect their most sensitive data.

*Spring Cloud Oracle* provides tools and services to integrate Oracle Cloud Infrastructure and Software with the Spring ecosystem in an idiomatic and flexible manner. It is designed to simplify microservices development that uses Oracle Database, middleware, and messaging on Oracle and non-Oracle clouds as well as on-premises.     

Spring Cloud Oracle 1.2.0 is the most recent release which brings together in one place Spring Boot Starters, autoconfiguration and sample code for Oracle Database and OCI services.

Spring Cloud Oracle goes beyond simply wrapping the OCI APIs in starters and adds idiomatic ways to integrate with Spring including for example the ability to use OCI Vault as a Spring Property Source and OCI Object Storage as a Spring Resource provider. We also provide Spring CLI integration with a project catalog to help you create Spring Boot projects using Oracle.

The following sub-projects are included in Spring Cloud Oracle:

| Project                                                                                                           | Description                                                            |
|-------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------|
| [Spring Cloud OCI](./spring-cloud-oci/README.md)                                                                  | Use OCI services with well-known Spring idioms and APIs                |
| [Oracle Database Spring Boot Starters](./database/starters/README.md)                                             | Integrate Oracle Database with Spring Boot                             |
| [Spring Cloud Stream Binder for Oracle TxEventQ](./database/spring-cloud-stream-binder-oracle-txeventq/README.md) | Build highly scalable event-driven microservices with Oracle TxEventQ. |

## Some key features

- Compatible with Spring Boot 3.2 and 3.3
- Support for several common OCI services including Object Storage, Functions, Logging, Notifications, Queues, Streaming, Email Delivery, Vault, Autonomous Database and Generative AI (for embeddings and inferencing)
- Improved autoconfiguration for Spring Boot Starters for Oracle Database, and support for Oracle Database 23ai
- OCI service components are built on top of OCI SDK for Java and a core module provides OCI configuration and authentication support


## Documentation

We encourage you to learn more in the  Spring Cloud Oracle documentation:

| Version                   | Reference Docs                                                                                 | API Docs                                                                           |
|---------------------------|------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| Spring Cloud Oracle 1.0.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.0.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.0.0/javadocs/index.html) |
| Spring Cloud Oracle 1.0.1 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.0.1/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.0.1/javadocs/index.html) |
| Spring Cloud Oracle 1.1.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.1.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.1.0/javadocs/index.html) |
| Spring Cloud Oracle 1.2.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.2.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.2.0/javadocs/index.html) |
| Spring Cloud Oracle 1.3.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.3.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.3.0/javadocs/index.html) |

Additionally, you can explore sample applications for each module in the [samples directory](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples).

## Roadmap

Spring Cloud Oracle has a strong and constantly evolving roadmap as we work to provide comprehensive up-to-date coverage.  Right now, we are working on the following updates: 

- Spring Cloud Stream Binder for Oracle Transactional Event Queues
- A Spring Boot Starter for JSON Collections in the Oracle Database
- A Spring Boot Starter for OKafka and TxEventQ
- OpenRewrite recipes to ease migration from HikariCP to Oracle Universal Connection Pool
- Further simplification of running Spring Boot applications as OCI Functions, including as native images using GraalVM Native Image compilation
- Further improvement of the idiomaticity of OCI Service modules and the expansion of adoption of the Spring Template pattern
- Additional Spring CLI project templates and example applications, particularly in the Spring AI space, focusing on retrieval augmented generation and agentic use cases

We’d love to hear from you! Please let us know about your experiences using Spring Cloud Oracle, and what else you would like to see supported by opening an issue in our GitHub repository.

## More great stuff to check out

If you are building Spring Boot applications with Oracle Database, you should also check out [Oracle Backend for Spring Boot and Microservices](https://bit.ly/oraclespringboot) which simplifies the task of building, testing, and operating microservices platforms for reliable, secure, and scalable enterprise applications on any cloud or on your own infrastructure.

If you'd like to try it out yourself, [CloudBank AI](https://bit.ly/cloudbankAI) is a great way to learn more.  It's a free self-paced hands-on lab that shows you how to build Spring Boot microservices and covers topics like REST, asynchronous services with JMS, storing data in Oracle, service dsicovery, using Spring Actuator for monitoring, Prometheus, Grafana, Loki, OpenTelemetry and Jaeger, implementing the saga pattern with Long Running Actions, exposing services using APISIX API Gateway and building an AI ChatBot using Ollama.

## Contributing

This project welcomes contributions from the community. Before submitting a pull request, please [review our contribution guide](./CONTRIBUTING.md)

## Security

Please consult the [security guide](./SECURITY.md) for our responsible security vulnerability disclosure process

## License

Copyright (c) 2023, 2024, Oracle and/or its affiliates.

Released under the Universal Permissive License v1.0 as shown at
<https://oss.oracle.com/licenses/upl/>.
