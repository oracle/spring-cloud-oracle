# Oracle Spring Boot Sample for JSON Duality Views

This sample application demonstrates how to use the Oracle Spring Boot Starter JSON Collections with [JSON Duality Views](https://docs.oracle.com/en/database/oracle/oracle-database/23/jsnvu/overview-json-relational-duality-views.html)

The Oracle Spring Boot Sample for JSON Duality Views package includes the following components to demonstrate development with JSON Duality Views from a Spring Boot Java context:

- Entities for JSON duality views (Student, Enrollment, Course, Lecture Hall)
- Services to interact with the JSON duality views
- A SQL script that initializes the database, including the JSON duality views.
- A comprehensive test that uses Spring Boot services to manipulate data from JSON duality views.

## Run the sample application

The sample application creates a temporary Oracle Free container database, and requires a docker runtime environment.

To run the test application, run the following command:

```shell
mvn test
```

## Configure your project to use Oracle JSON Duality Views

To use Oracle JSON Duality Views from your Spring Boot application, add the following Maven dependency to your project:

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-json-collections</artifactId>
</dependency>
```
