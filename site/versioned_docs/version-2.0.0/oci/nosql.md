---
title: Oracle NoSQL Database
sidebar_position: 12
---

# Oracle NoSQL Database

[Oracle NoSQL Database](https://docs.oracle.com/en-us/iaas/nosql-database/index.html) provides managed JSON, table, and key-value storage with flexible transaction guarantees.

## Dependency Coordinates

### Maven

```xml
<dependency>
  <groupId>com.oracle.cloud.spring</groupId>
  <artifactId>spring-cloud-oci-starter-nosql</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("com.oracle.cloud.spring:spring-cloud-oci-starter-nosql")
}
```

## Using NoSQL Repositories

The starter auto-configures `NosqlDBConfig` so you can use repository-based access.

```yaml
spring:
  cloud:
    oci:
      config:
        type: file
      region:
        static: us-ashburn-1
```

Enable repositories:

```java
import com.oracle.nosql.spring.data.config.AbstractNosqlConfiguration;
import com.oracle.nosql.spring.data.repository.config.EnableNosqlRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableNosqlRepositories
public class MyApp extends AbstractNosqlConfiguration {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}
```

Define repository and entity types:

```java
public interface BookRepository extends NosqlRepository<Book, Long> {
    Iterable<Book> findByAuthor(String author);
    Iterable<Book> findByTitle(String title);
}
```

```java
@NosqlTable(tableName = "books")
public class Book {
    @NosqlId(generated = true)
    long id;
    String title;
    String author;
    double price;
}
```

## Configuration

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.cloud.oci.nosql.enabled` | Enables the NoSQL DB configuration | No | `true` |

## Sample

See [`spring-cloud-oci-nosql-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-nosql-sample).
