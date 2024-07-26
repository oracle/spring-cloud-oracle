# Spring Cloud Oracle

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green)

## Introduction

Spring Cloud Oracle, eases the integration with Oracle Database and OCI services with the help of OCI Java SDK internally. It offers a convenient way to interact with Oracle Database and OCI-provided services using well-known Spring idioms and APIs, such as the messaging or storage API. Developers can build their applications around the hosted services without having to care about infrastructure or maintenance. Spring Cloud Oracle contains autoconfiguration support for Oracle Database and OCI-managed services.

All Spring Cloud Oracle artifacts are made available through Maven Central. With Spring Cloud Oracle, developers only need to add some annotations and a small number of configuration parameters to connect Spring Cloud applications to Oracle Database and OCI services.

## Spring Cloud Oracle documentation

For a deep dive into the project, refer to the Spring Cloud Oracle documentation:

| Version                   | Reference Docs                                                                                 | API Docs                                                                           |
|---------------------------|------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| Spring Cloud Oracle 1.0.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.0.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.0.0/javadocs/index.html) |
| Spring Cloud Oracle 1.0.1 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.0.1/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.0.1/javadocs/index.html) |
| Spring Cloud Oracle 1.1.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.1.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.1.0/javadocs/index.html) |
| Spring Cloud Oracle 1.2.0 | [Reference Docs](https://oracle.github.io/spring-cloud-oracle/1.2.0/reference/html/index.html) | [API Docs](https://oracle.github.io/spring-cloud-oracle/1.2.0/javadocs/index.html) |

## Compatibility with Spring Project Versions

This project has dependency and transitive dependencies on Spring Projects. The table below outlines the versions of Spring Cloud, Spring Boot and Spring Framework versions that are compatible with certain Spring Cloud Oracle version.

| Spring Cloud Oracle | Spring Cloud | Spring Boot  | OCI Java SDK |
|---------------------|--------------|--------------|--------------|
| 1.0.0               | 2022.0.x     | 3.1.x, 3.0.x | 3.24.0       |
| 1.0.1               | 2023.0.x     | 3.2.x        | 3.44.3       |
| 1.1.0               | 2023.0.x     | 3.2.x        | 3.44.x       |
| 1.2.0               | 2023.0.x     | 3.2.x        | 3.44.x       |

## Sample applications

Samples for each service supported by Spring Cloud Oracle below:

* [Application Samples](spring-cloud-oci-samples)

## Checking out and building

If you would like to clone this repo in your OCI tenancy, click on 'Open in Code Editor' button below to clone and launch OCI Code Editor for this sample.

[<img src="https://raw.githubusercontent.com/oracle-devrel/oci-code-editor-samples/main/images/open-in-code-editor.png" />](https://cloud.oracle.com/?region=home&cs_repo_url=https://github.com/oracle/spring-cloud-oci.git&cs_open_ce=true&cs_readme_path=spring-cloud-oci-samples/spring-cloud-oci-storage/README.md)

or

To check out the project manually and build it from source, do the following:

```shell
git clone https://github.com/oracle/spring-cloud-oracle.git
cd spring-cloud-oci
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

## Contributing

This project welcomes contributions from the community. Before submitting a pull request, please [review our contribution guide](./CONTRIBUTING.md)

## Security

Please consult the [security guide](./SECURITY.md) for our responsible security vulnerability disclosure process

## License

Copyright (c) 2023, 2024, Oracle and/or its affiliates.

Released under the Universal Permissive License v1.0 as shown at
<https://oss.oracle.com/licenses/upl/>.
