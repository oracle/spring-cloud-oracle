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
export
export
export

mvn spring-boot:run
```

Execute the shell script running the following command:

```shell
source run-app.sh
```