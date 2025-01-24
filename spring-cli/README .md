# Spring CLI integration

Spring CLI integration with a project catalog to help you create Spring Boot projects using Oracle and [Oracle Backend for Microservices and AI](https://bit.ly/OracleAI-microservices).

## Install Spring CLI

The goal of the Spring CLI is to increase your productivity when you create new projects and when you add functionality to existing projects. [Spring CLI documentation](https://docs.spring.io/spring-cli/reference/index.html) describes how to install the CLI on various platforms.

## Add to Spring CLI

Execute the following command to add the `project-catalog` for [Oracle Backend for Microservices and AI](https://bit.ly/OracleAI-microservices).

```shell
spring add.....
```

The Spring CLI is now aware of the [Oracle Backend for Microservices and AI](https://bit.ly/OracleAI-microservices) integration so when building a new project (or adding to a current one) you can use the command `spring .....`

## Run the Spring CLI application locally

To run the application locally you need access to an Oracle Database (remotly or locally). Create a shell script with the following content called `run-app.sh`

```shell
#!/bin/bash
export spring_datasource_url=<URL to database>
export liquibase_datasource_username=<Liquibase database user>
export liquibase_datasource_password=<Liquibase database user password>
export spring_datasource_username=<Application database user>
export spring_datasource_password=<Application database user password>
export otel_exporter_otlp_endpoint=http://localhost:8080 # Fake URL
mvn spring-boot:run -DskipTests
```

You need to turn off registering to Eureka (unless you have an instance you can connect to). Set `eureka.client.enabled` to `false` and turn off exporting of tracing. Set `management.otlp.tracing.export.enabled` to `false`. The values are set in the `application.yaml` file. For example:

```yaml
eureka:
  instance:
    hostname: ${spring.application.name}
    preferIpAddress: true
  client:
    service-url:
      defaultZone: ${eureka.service-url}
    fetch-registry: true
    register-with-eureka: true
    enabled: false
```

```yaml
management:
  otlp:
    tracing:
      endpoint: ${otel.exporter.otlp.endpoint
      export:
        enabled: false
```

Execute the shell script running the following command:

```shell
source run-app.sh
```

You can see a few `WARNINGS` with this message which can be ignored:

```log
WARNING:

Liquibase detected the following invalid LIQUIBASE_* environment variables:
```