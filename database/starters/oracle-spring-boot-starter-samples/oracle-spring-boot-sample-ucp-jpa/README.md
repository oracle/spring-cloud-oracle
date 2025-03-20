# Oracle Spring Boot Sample UCP JPA

This sample application demonstrates how to use the Oracle Spring Boot Starter UCP with Spring Data JPA, connecting your Oracle Database with powerful ORM abstractions that facilitate rapid development.

The Oracle Spring Boot Sample UCP JPA package includes a JPA entity, repository, and rest controller to interact with the JPA repository. All necessary configuration and dependencies are bootstrapped, with an end-to-end test demonstrating the functionality of Spring JPA with Oracle Database and UCP.

## Run the sample application

The sample application test uses Testcontainers, and creates a temporary Oracle Free container database, and requires a docker runtime environment. The sample application demonstrates the use of Spring Data JPA with the Oracle Spring Boot Starter UCP.

To run the test application, run the following command:

```shell
mvn test
```

## Configure Maven dependencies to use Oracle UCP and Spring Data JPA

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-ucp</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

## Configure application properties to use Oracle UCP as the datasource provider

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none
  datasource:
    username: ${USERNAME}
    password: ${PASSWORD}
    url: ${JDBC_URL}

    # Set these to use UCP over Hikari.
    driver-class-name: oracle.jdbc.OracleDriver
    type: oracle.ucp.jdbc.PoolDataSourceImpl
    oracleucp:
      initial-pool-size: 1
      min-pool-size: 1
      max-pool-size: 30
      connection-pool-name: UCPSampleApplication
      connection-factory-class-name: oracle.jdbc.pool.OracleDataSource
```

## Write a JPA repository and entity

```java
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, String> {}
```

```java
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "STUDENT")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    private String email;
    private String major;
    private double credits;
    private double gpa;
```