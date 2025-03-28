# Spring CLI integration

Spring CLI integration with a project catalog to help you create Spring Boot projects using Oracle and [Oracle Backend for Microservices and AI](https://bit.ly/OracleAI-microservices).

## Install Spring CLI

The goal of the Spring CLI is to increase your productivity when you create new projects and when you add functionality to existing projects. [Spring CLI documentation](https://docs.spring.io/spring-cli/reference/index.html) describes how to install the CLI on various platforms.

## Add to Spring CLI

Execute the following command to add the `project-catalog` for [Oracle Backend for Microservices and AI](https://bit.ly/OracleAI-microservices).

```shell
spring project-catalog add --name obaas --url https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cli/catalog
```

The Spring CLI is now aware of the [Oracle Backend for Microservices and AI](https://bit.ly/OracleAI-microservices) integration.

## Create project

To create a new project use the following command (there are more options available):

```shell
spring boot new --from obaas --name myproject
```

## Run the Spring CLI application locally

To run the application locally you need access to an Oracle Database (remotely or locally). Create a shell script with the following content called `run-app.sh` and set the values for the variables to reflect your environment.

```shell
#!/bin/bash
export spring_datasource_url=<URL to database>
export liquibase_datasource_username=<Liquibase database user>
export liquibase_datasource_password=<Liquibase database user password>
export spring_datasource_username=<Application database user>
export spring_datasource_password=<Application database user password>
export otel_exporter_otlp_endpoint=http://localhost:8080 # Dummy URL
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The Spring profile `local` turns off Eureka discovery and OTLP tracing.

Execute the shell script running the following command:

```shell
source run-app.sh
```

You can see a few `WARNINGS` with this message which can be ignored:

```log
WARNING:

Liquibase detected the following invalid LIQUIBASE_* environment variables:
```
