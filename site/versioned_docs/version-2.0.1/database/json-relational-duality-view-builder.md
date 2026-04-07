---
title: JSON Relational Duality View Builder
sidebar_position: 5
---

Oracle AI Database’s JSON Relational Duality Views (or simply duality views) let you treat relational tables and JSON documents as two sides of the same model: build your relational schema, and use comprehensive, normalized JSON documents on top.

The JSON Relational Duality View builder allows developers to automatically generate duality view DDL from plain Java classes or JPA entities.

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.database.spring</groupId>
  <artifactId>oracle-spring-boot-json-relational-duality-views</artifactId>
</dependency>
```

## Enable Duality View Builder with package scanning

The Duality View builder must be explicitly enabled through package scanning.

```java
@SpringBootApplication(scanBasePackages = {
        "com.example",
        // Enable the duality view event listener
        "com.oracle.spring.json.duality.builder"
})
public class Application { // main class }
```

## Annotations for Duality View Generation

The custom `@JsonRelationalDualityView `annotation denotes that a Java class (usually a JPA entity) should have a duality view generated from its structure. Fields and annotations on the class are used to dynamically construct the duality view.

You may apply this annotation to any nested classes to create nested objects in your view.

The custom `@AccessMode` annotation is used to specify insert, update, and delete functionality on view objects. By default, read-only access is granted in the generated view.

The jakarta.json `@JsonbProperty("_id")` annotation is recommended for any root ID fields: duality views use the _id field in JSON documents for the root primary key. It may also be used to rename class fields in the resulting duality view.

The jakarta.json `@JsonbTransient` annotation is recommended to skip specific fields in the generated duality view. This is necessary for cyclic objects.

## Annotate Java Classes or JPA Entities

Any Java class or JPA entity annotated with `@JsonRelationalDualityView` on the Spring Boot classpath scan will have a duality view generated from it's metadata.

For example, the `Actor` entity is annotated with `@JsonRelationalDualityView`, and a duality view generated at startup time, but after Hibernate runs:

```java
@Entity
@Table(name = "actor")
@JsonRelationalDualityView(accessMode = @AccessMode(insert = true))
public class Actor {
    @JsonbProperty(_ID_FIELD)
    @Id
    @Column(name = "actor_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actorId;
    
    // other entity fields, Movies, Directors, etc.
```

A resulting duality view definition from this JPA entity may look like this:

```sql
create force editionable json relational duality view actor_dv as actor @insert {
  _id : actor_id
  firstName : first_name
  lastName : last_name
  movies : movie_actor @insert [ {
    movie @unnest @insert {
      _id : movie_id
      title
      releaseYear : release_year
      genre
      director @insert @link (from : [director_id]) {
        _id : director_id
        firstName : first_name
        lastName : last_name
      }
    }
  } ]
}
```

## Control generation through Spring properties

We can configure duality view generation in Spring JPA settings. The same ddl-auto values for JPA can be used for duality views:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    dv:
      # create-drop views after JPA ddl-auto is complete
      ddl-auto: create-drop
      # Print JSON Relational Duality Views to the console
      show-sql: true
```

## Learn By Example

- [JPA Duality View Builder Sample with Actor, Movie, and Director entities](https://github.com/anders-swanson/oracle-database-code-samples/tree/main/json/jpa-duality-views)