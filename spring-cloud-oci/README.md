# Spring Cloud Oracle

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green)

## Introduction

Spring Cloud Oracle eases the integration with Oracle CLoud Infrastructure (OCI), offering a convenient way to interact with OCI services using well-known Spring idioms and APIs, such as the messaging or storage API. Developers can build their applications around the hosted services without having to care about infrastructure or maintenance.

## Spring Cloud Oracle documentation

For a deep dive into the project, refer to the Spring Cloud Oracle documentation:

| Version                   | Reference Docs                                                                                 | API Docs                                                                           |
|---------------------------|------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| Spring Cloud Oracle 1.0.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.0.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.0.0/javadocs/index.html) |
| Spring Cloud Oracle 1.0.1 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.0.1/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.0.1/javadocs/index.html) |
| Spring Cloud Oracle 1.1.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.1.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.1.0/javadocs/index.html) |
| Spring Cloud Oracle 1.2.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.2.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.2.0/javadocs/index.html) |
| Spring Cloud Oracle 1.3.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.3.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.3.0/javadocs/index.html) |

## Compatibility with Spring Project Versions

This project has dependency and transitive dependencies on Spring Projects. The table below outlines the versions of Spring Cloud, Spring Boot and Spring Framework versions that are compatible with certain Spring Cloud Oracle version.

| Spring Cloud Oracle | Spring Cloud | Spring Boot  | OCI Java SDK |
|---------------------|--------------|--------------|--------------|
| 1.0.0               | 2022.0.x     | 3.1.x, 3.0.x | 3.24.0       |
| 1.0.1               | 2023.0.x     | 3.2.x        | 3.44.3       |
| 1.1.0               | 2023.0.x     | 3.2.x        | 3.44.x       |
| 1.2.0               | 2023.0.x     | 3.2.x        | 3.44.x       |
| 1.3.0               | 2023.0.x     | 3.2.x        | 3.44.x       |
| 1.4.0               | 2024.0.x     | 3.4.x        | 3.55.1       |

## Sample applications

Samples for each service supported by Spring Cloud Oracle below:

* [Application Samples](spring-cloud-oci-samples)

## Checking out and building

If you would like to clone this repo in your OCI tenancy, click on 'Open in Code Editor' button below to clone and launch OCI Code Editor for this sample.

[<img src="https://raw.githubusercontent.com/oracle-devrel/oci-code-editor-samples/main/images/open-in-code-editor.png" />](https://cloud.oracle.com/?region=home&cs_repo_url=https://github.com/oracle/spring-cloud-oracle.git&cs_open_ce=true)

or

To check out the project manually and build it from source, do the following:

```shell
git clone https://github.com/oracle/spring-cloud-oracle.git
cd spring-cloud-oracle
mvn package
```

To build and install jars into your local Maven cache:

```shell
mvn install
```

For faster builds, we recommend using [Maven Daemon](https://github.com/apache/maven-mvnd) and using following commands:

Build:

```shell
make build
```

*Note*: If you get a coverage error, please ensure you have up to date jacoco files by running a `clean` first, then run `package`,  `install`, or `build` again.

Clean:

```shell
make clean
```

## Documentation

Java docs can be generated with below command

```shell
make javadocs
```

JavaDocs generated at `target/site/`
