---
title: Email Delivery
sidebar_position: 10
---

# Email Delivery

[OCI Email Delivery](https://docs.oracle.com/en-us/iaas/Content/Email/home.htm) provides Spring Mail integrations through `MailSender` and `JavaMailSender`.

## Dependency Coordinates

### Maven

```xml
<dependency>
  <groupId>com.oracle.cloud.spring</groupId>
  <artifactId>spring-cloud-oci-starter-email</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("com.oracle.cloud.spring:spring-cloud-oci-starter-email")
}
```

## SMTP Configuration

Email Delivery uses standard Spring Mail SMTP configuration:

```yaml
spring:
  mail:
    host: ${OCI_SMTP_HOST}
    username: ${OCI_SMTP_USERNAME}
    password: ${OCI_SMTP_PASSWORD}
    port: 587
```

Create SMTP credentials in OCI and use the OCI Email Delivery SMTP endpoint for `spring.mail.host`.

## Using `MailSender`

```java
@Service
public class EmailService {
    private final MailSender mailSender;

    public EmailService(@Qualifier("ociMailSender") MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSimpleMail(String from, String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
```

## Using `JavaMailSender`

```java
@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    public EmailService(@Qualifier("ociJavaMailSender") JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendJavaMail(
            String from,
            String to,
            String subject,
            String text,
            File fileAttachment) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        Multipart multipartContent = new MimeMultipart();
        MimeBodyPart textContent = new MimeBodyPart();
        textContent.setContent(text, "text/html");
        multipartContent.addBodyPart(textContent);

        MimeBodyPart attachmentContent = new MimeBodyPart();
        DataSource source = new FileDataSource(fileAttachment);
        attachmentContent.setDataHandler(new DataHandler(source));
        attachmentContent.setFileName(fileAttachment.getName());
        multipartContent.addBodyPart(attachmentContent);

        message.setFrom(from);
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(multipartContent);
        javaMailSender.send(message);
    }
}
```

## Sample

See [`spring-cloud-oci-email-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-email-sample).
