# OCI Logging Service - Spring Framework Sample

This sample application demonstrates how to use the OCI Logging Spring Cloud APIs to:

* Ingest logs into OCI Logging Service

This application has below classes:

* `LoggingController`- REST Container class which contains the REST APIs for performing each of the operations in the above section
* `SpringCloudOciLoggingSampleApplication` - Spring Boot application class, which when run will launch the application

## Prerequisites
Configuration needed to run the Application to be configured in application.properties

* spring.cloud.oci.region.static - OCI Region name(Ex: us-phoenix-1) where the OCI resources needs to be created
* spring.cloud.oci.compartment.static - OCID of the OCI Compartment where the OCI resources needs to be created
* spring.cloud.oci.config.type - Authentication type to be used for OCI. It could be either of RESOURCE_PRINCIPAL, INSTANCE_PRINCIPAL, SIMPLE and FILE. If nothing is specified, FILE type is used by default
* spring.cloud.oci.config.file - The file path set to this property will be used as config file for FILE type authentication which used OCI config file. If nothing is specified, OCI config file from user home directory will be used
* spring.cloud.oci.config.profile - Profile to be used in the OCI config file for Authentication. By default DEFAULT profile will be used
* spring.cloud.oci.logging.logId - OCID of OCI Log where the logs need to be injested

If the spring.cloud.oci.config.type is SIMPLE, then below properties also needs to be set in the application.properties

* spring.cloud.oci.config.userId
* spring.cloud.oci.config.tenantId
* spring.cloud.oci.config.fingerprint
* spring.cloud.oci.config.privateKey
* spring.cloud.oci.config.passPhrase
* spring.cloud.oci.config.region

Please refer [OCI SDK Authentication Methods
](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdk_authentication_methods.htm) for more details on the Authentication types supported by OCI

## Quick Launch

If you like to try out this sample in your OCI tenancy, click on 'Open in Code Editor' button below to clone and launch OCI Code Editor for this sample.

[<img src="https://raw.githubusercontent.com/oracle-devrel/oci-code-editor-samples/main/images/open-in-code-editor.png" />](https://cloud.oracle.com/?region=home&cs_repo_url=https://github.com/oracle/spring-cloud-oci.git&cs_open_ce=true&cs_readme_path=spring-cloud-oci-samples/spring-cloud-oci-storage/README.md)

or You may clone the repository manually with below instructions.

```
git clone https://github.com/oracle/spring-cloud-oci.git spring-cloud-oci
```

## Let's start

1. Run `mvn clean install` from root directory of the repository code.
2. To start the application, run the below command from sample root directory.
```
mvn spring-boot:run
```

Note: Default service port is `8080`. You may change this with `server.port` property.

## Try out sample

The base URL for all the APIs exposed in this application is http://localhost:8080//demoapp/api/logging/

Using the above base URL, below APIs can be invoked:

|API | Method | URI | Request Params|
|:-------|:--------|:------|:-------|
| Put Logs | POST | putlogs | logText |

## References
* [OCI Logging Service](https://docs.oracle.com/en-us/iaas/Content/Logging/home.htm)
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
