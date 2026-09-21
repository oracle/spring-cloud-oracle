---
title: Vault
sidebar_position: 5
---

# Vault

[OCI Vault](https://docs.oracle.com/en-us/iaas/Content/KeyManagement/home.htm) can be used both as a Spring property source and as an application bean for managing secrets.

## Dependency Coordinates

### Maven

```xml
<dependency>
  <groupId>com.oracle.cloud.spring</groupId>
  <artifactId>spring-cloud-oci-starter-vault</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("com.oracle.cloud.spring:spring-cloud-oci-starter-vault")
}
```

## Using Vault as a Property Source

Secrets can be loaded dynamically into the Spring environment:

```yaml
spring:
  cloud:
    oci:
      config:
        type: file
      region:
        static: us-ashburn-1
      vault:
        enabled: true
        compartment: ${OCI_COMPARTMENT_ID}
        property-refresh-interval: 10000ms
        property-sources:
          - vault-id: ${OCI_VAULT_ID}
```

```java
@Value("${secretname}")
String secretValue;
```

## Using `VaultTemplate`

The starter also auto-configures a `VaultTemplate` bean for secret operations.

```yaml
spring:
  cloud:
    oci:
      config:
        type: file
      region:
        static: us-ashburn-1
      vault:
        compartment: ${OCI_COMPARTMENT_ID}
        vault-id: ${OCI_VAULT_ID}
        enabled: true
```

```java
@Autowired
private VaultTemplate vaultTemplate;

public String getSecretByName(String secretName) {
    GetSecretBundleByNameResponse bundle = vaultTemplate.getSecret(secretName);
    return vaultTemplate.decodeBundle(bundle);
}
```

## Configuration

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.cloud.oci.vault.enabled` | Enables the OCI Vault APIs | No | `true` |
| `spring.cloud.oci.vault.compartment` | Compartment for Vault APIs and property sources | Yes |  |
| `spring.cloud.oci.vault.vault-id` | Vault OCID for Vault APIs | Yes |  |
| `spring.cloud.oci.vault.property-refresh-interval` | Refresh interval for property reload | No | `10m` |
| `spring.cloud.oci.vault.property-sources` | List of Vaults to use as property sources | No |  |
| `spring.cloud.oci.vault.property-sources[i].vault-id` | Vault OCID for a property source entry | Yes |  |

## Sample

See [`spring-cloud-oci-vault-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-vault-sample).
