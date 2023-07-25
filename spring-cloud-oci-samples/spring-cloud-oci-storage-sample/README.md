# OCI Storage - Spring Cloud OCI Sample

This sample demonstrates how you can get started quickly with Spring Cloud OCI to work with Oracle Cloud Infrastructre (OCI) Storage Service. This sample implements simple REST service which internally uses Spring Cloud OCI Storage APIs.

## What is Spring Cloud OCI?

Spring Cloud for OCI, eases the integration with OCI services with the help of OCI Java SDK internally. It offers a convenient way to interact with OCI-provided services using well-known Spring idioms and APIs, such as the messaging or storage API. Developers can build their applications around the hosted services without having to care about infrastructure or maintenance. Spring Cloud for OCI contains auto-configuration support for OCI-managed services.

## What is OCI Storage?

Oracle Cloud Infrastructure provides customers with high-performance computing and low-cost cloud storage options. Through on-demand local, object, file, block, and archive storage, Oracle Cloud addresses key storage workload requirements and use cases. Customers can use the storage gateway and data transfer service to safely and securely move their data to the cloud.

## Quick Launch

If you like to try out this sample in your OCI tenancy, click on 'Open in Code Editor' button below to clone and launch OCI Code Editor for this sample.

[<img src="https://raw.githubusercontent.com/oracle-devrel/oci-code-editor-samples/main/images/open-in-code-editor.png" />](https://cloud.oracle.com/?region=home&cs_repo_url=https://github.com/oracle/spring-cloud-oci.git&cs_open_ce=true&cs_readme_path=spring-cloud-oci-samples/spring-cloud-oci-storage/README.md)

or You may clone the repository manually with below instructions.

```
git clone https://github.com/oracle/spring-cloud-oci.git spring-cloud-oci
```

## Let's start

1. Run `mvn clean install` from root directory of the repository code.

2. Set appropriate values at `application.properties` for the below properties. (Refer Spring Cloud OCI docs for more details.)
```
spring.cloud.oci.region.static = US_ASHBURN_1
spring.cloud.oci.compartment.static = <COMPARTMENT_OCID>
```

3. To start the application, run the below command from sample root directory.
```
mvn spring-boot:run
```

Note: Default service port is `8080`. You may change this with `server.port` property.

## Sample Application API Reference

Launch Swagger UI (http://localhost:8080/swagger-ui/index.html) to view all available APIs and their payload samples.

![Swagger UI](./images/swagger-ui.png)

## References
* [Spring Cloud OCI - Documentation](#)
* [Spring Cloud OCI - Open Source](https://github.com/oracle/spring-cloud-oci)
* [OCI SDK - Documentation](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdks.htm)

## Contributing
This project is open source.  Please submit your contributions by forking this repository and submitting a pull request!  Oracle appreciates any contributions that are made by the open source community.

## License
Copyright (c) 2023 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](../../LICENSE.txt) for more details.

ORACLE AND ITS AFFILIATES DO NOT PROVIDE ANY WARRANTY WHATSOEVER, EXPRESS OR IMPLIED, FOR ANY SOFTWARE, MATERIAL OR CONTENT OF ANY KIND CONTAINED OR PRODUCED WITHIN THIS REPOSITORY, AND IN PARTICULAR SPECIFICALLY DISCLAIM ANY AND ALL IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE.  FURTHERMORE, ORACLE AND ITS AFFILIATES DO NOT REPRESENT THAT ANY CUSTOMARY SECURITY REVIEW HAS BEEN PERFORMED WITH RESPECT TO ANY SOFTWARE, MATERIAL OR CONTENT CONTAINED OR PRODUCED WITHIN THIS REPOSITORY. IN ADDITION, AND WITHOUT LIMITING THE FOREGOING, THIRD PARTIES MAY HAVE POSTED SOFTWARE, MATERIAL OR CONTENT TO THIS REPOSITORY WITHOUT ANY REVIEW. USE AT YOUR OWN RISK. 
