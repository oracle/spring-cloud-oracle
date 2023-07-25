# OCI Email Serivce - Spring Framework Sample

This sample demonstrates how you can get started quickly with Spring Framework to work with Oracle Cloud Infrastructre (OCI) Email Service. This sample implements simple REST service.

This project sample showcases the sample code to send email using OCI Email Services via HTTP through an Spring Boot application. This project contains all the required OCI SDK dependencies in `pom.xml`.

## Prerequisites
* Create Approved Senders in Email Delivery. [[Refer Here]](https://docs.oracle.com/en-us/iaas/Content/Email/Tasks/managingapprovedsenders.htm)
Note: For non-prod testing, you may use `noreply@notification.<region>.oci.oraclecloud.com` example `noreply@notification.us-ashburn-1.oci.oraclecloud.com`.
* Create SMTP Credentials [[Refer Here]](https://docs.oracle.com/en-us/iaas/Content/Identity/Tasks/managingcredentials.htm#Working3)
* Get the SMTP hostname from "Public Endpoint" under SMTP configuration [[Refer here]](https://docs.oracle.com/en-us/iaas/Content/Email/Reference/gettingstarted_topic-Configure_the_SMTP_connection.htm)


## Quick Launch

If you like to try out this sample in your OCI tenancy, click on 'Open in Code Editor' button below to clone and launch OCI Code Editor for this sample.

[<img src="https://raw.githubusercontent.com/oracle-devrel/oci-code-editor-samples/main/images/open-in-code-editor.png" />](https://cloud.oracle.com/?region=home&cs_repo_url=https://github.com/oracle/spring-cloud-oci.git&cs_open_ce=true&cs_readme_path=spring-cloud-oci-samples/spring-cloud-oci-storage/README.md)

or You may clone the repository manually with below instructions.

```
git clone https://github.com/oracle/spring-cloud-oci.git spring-cloud-oci
```

## Let's start

1. Run `mvn clean install` from root directory of the repository code.

2. Set/add appropriate values at `application.properties` for the below properties. (Refer Spring Cloud OCI docs for more details.)
```
spring.mail.host=${MAIL_SMTP_HOST}
spring.mail.username=${MAIL_SMTP_USER}
spring.mail.password=${MAIL_SMTP_PASSWORD}
```

You may also pass these values as environment variables.

Note: Consider handling password in a secured manner. This is just an example. (Not for production use).

3. To start the application, run the below command from sample root directory.
```
mvn spring-boot:run
```

Note: Default service port is `8080`. You may change this with `server.port` property.

## Try out sample

Send POST request to `http://localhost:8080/send` with below body contents.

```
{
    "from": "<FROM_EMAIL_ADDRESS>",
    "to": "<YOUR_EMAIL>",
    "subject": "Test subject",
    "text": "Hello World - Sample Body Content"
}
```

## References
* [Step by Step Instructions for OCI Email Service](https://blogs.oracle.com/cloud-infrastructure/post/step-by-step-instructions-to-send-email-with-oci-email-delivery)
* [OCI SDK - Documentation](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdks.htm)

## Contributing
This project is open source.  Please submit your contributions by forking this repository and submitting a pull request!  Oracle appreciates any contributions that are made by the open source community.

## License
Copyright (c) 2023 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](../../LICENSE.txt) for more details.

ORACLE AND ITS AFFILIATES DO NOT PROVIDE ANY WARRANTY WHATSOEVER, EXPRESS OR IMPLIED, FOR ANY SOFTWARE, MATERIAL OR CONTENT OF ANY KIND CONTAINED OR PRODUCED WITHIN THIS REPOSITORY, AND IN PARTICULAR SPECIFICALLY DISCLAIM ANY AND ALL IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE.  FURTHERMORE, ORACLE AND ITS AFFILIATES DO NOT REPRESENT THAT ANY CUSTOMARY SECURITY REVIEW HAS BEEN PERFORMED WITH RESPECT TO ANY SOFTWARE, MATERIAL OR CONTENT CONTAINED OR PRODUCED WITHIN THIS REPOSITORY. IN ADDITION, AND WITHOUT LIMITING THE FOREGOING, THIRD PARTIES MAY HAVE POSTED SOFTWARE, MATERIAL OR CONTENT TO THIS REPOSITORY WITHOUT ANY REVIEW. USE AT YOUR OWN RISK. 
