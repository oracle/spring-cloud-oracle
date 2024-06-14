# OCI Queue Service - Spring Framework Sample

This sample application demonstrates how to use the Oracle Cloud Infrastructure (OCI) Queue Service Spring Cloud APIs to:

* Create an OCI Queue
* Get a queue
* List queues
* Delete a queue
* Put messages into a queue
* Get/consume messages from a queue
* Update messages in a queue
* Delete a message from a queue

This application has the following classes:

* `QueueController`- REST Container class which contains the REST APIs for performing each of the operations in the previous section.
* `SpringCloudOciQueueSampleApplication` - Spring Boot application class, which, when run, will launch the application.

## Prerequisites
Configuration items that are needed to run the Application can be configured in `application.properties`.

* `spring.cloud.oci.region.static` - OCI Region name(Ex: us-phoenix-1) where the OCI resources need to be created.
* `spring.cloud.oci.config.type` - Authentication type to be used for OCI. It could be one of the following: RESOURCEPRINCIPAL, INSTANCE_PRINCIPAL, SESSION_TOKEN, SIMPLE, FILE, and WORKLOAD_IDENTITY. If nothing is specified, FILE type is used by default.
* `spring.cloud.oci.config.file` - The file path set to this property will be used as the configuration file for FILE type authentication which uses the OCI configuration file. If nothing is specified, the OCI configuration file from the user's home directory will be used.
* `spring.cloud.oci.config.profile` - Profile to be used in the OCI config file for Authentication. If a profile is not specified, a DEFAULT profile will be used.

If the `spring.cloud.oci.config.type` is SIMPLE, then the following properties also need to be set in the `application.properties`.

* `spring.cloud.oci.config.userId`
* `spring.cloud.oci.config.tenantId`
* `spring.cloud.oci.config.fingerprint`
* `spring.cloud.oci.config.privateKey`
* `spring.cloud.oci.config.passPhrase`
* `spring.cloud.oci.config.region`

Refer to [OCI SDK Authentication Methods](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdk_authentication_methods.htm) for more details on the Authentication types supported by OCI.

## Quick Launch

To try out this sample in your OCI tenancy, click on the **Open in Code Editor** button below to clone and launch the OCI Code Editor for this sample.

[<img src="https://raw.githubusercontent.com/oracle-devrel/oci-code-editor-samples/main/images/open-in-code-editor.png" />](https://cloud.oracle.com/?region=home&cs_repo_url=https://github.com/oracle/spring-cloud-oci.git&cs_open_ce=true&cs_readme_path=spring-cloud-oci-samples/spring-cloud-oci-queue-sample/README.md)

You can also clone the repository manually with the following instructions.

```
git clone https://github.com/oracle/spring-cloud-oci.git spring-cloud-oci
```

## Let's start

1. Run `mvn clean install` from the root directory of the code repository.
2. To start the application, run the following command from the sample root directory.
```
mvn spring-boot:run
```

The default service port is `8080`. You can change this with the `server.port` property.

## Try the sample

The base URL for all the APIs exposed in this application is `http://localhost:8080/demoapp/api/queues`

Using the base URL, the following APIs can be invoked:

| API             | Method | URI                 | Request Params                                                             |
|:----------------|:-------|:--------------------|:---------------------------------------------------------------------------|
| Create Queue    | POST   |                     | queueName, compartmentId, deadLetterQueueDeliveryCount, retentionInSeconds |
| Get Queue       | GET    | /{queueId}          | queueId                                                                    |
| List Queues     | GET    |                     | queueName, compartmentId                                                   |
| Delete Queue    | DELETE | /{queueId}          | queueId                                                                    |
| Put Messages    | POST   | /{queueId}/messages | queueId, messages                                                          |
| Get Messages    | GET    | /{queueId}/messages | queueId, visibilityInSeconds, timeoutInSeconds, limit                      |
| Update Messages | PUT    | /{queueId}/messages | queueId, messageReceipts, visibilityInSeconds                              |
| Delete Message  | DELETE | /{queueId}/messages | queueId, messageReceipt                                                    |

## References
* [OCI Queue Service](https://docs.oracle.com/en-us/iaas/Content/queue/home.htm)
* [Spring Cloud OCI - Documentation](#)
* [Spring Cloud OCI - Open Source](https://github.com/oracle/spring-cloud-oci)
* [OCI SDK - Documentation](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdks.htm)

## Contributing
This project is open source.  Submit your contributions by forking this repository and submitting a pull request.  Oracle appreciates any contributions that are made by the open source community.

## License
Copyright (c) 2023 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](../../LICENSE.txt) for more details.

ORACLE AND ITS AFFILIATES DO NOT PROVIDE ANY WARRANTY WHATSOEVER, EXPRESS OR IMPLIED, FOR ANY SOFTWARE, MATERIAL OR CONTENT OF ANY KIND CONTAINED OR PRODUCED WITHIN THIS REPOSITORY, AND IN PARTICULAR SPECIFICALLY DISCLAIM ANY AND ALL IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE.  FURTHERMORE, ORACLE AND ITS AFFILIATES DO NOT REPRESENT THAT ANY CUSTOMARY SECURITY REVIEW HAS BEEN PERFORMED WITH RESPECT TO ANY SOFTWARE, MATERIAL OR CONTENT CONTAINED OR PRODUCED WITHIN THIS REPOSITORY. IN ADDITION, AND WITHOUT LIMITING THE FOREGOING, THIRD PARTIES MAY HAVE POSTED SOFTWARE, MATERIAL OR CONTENT TO THIS REPOSITORY WITHOUT ANY REVIEW. USE AT YOUR OWN RISK. 