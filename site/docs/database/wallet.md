---
title: Oracle AI Database Wallet
description: Configure Oracle AI Database Wallet and mutual TLS authentication for Spring Boot applications with the Oracle Wallet starter.
keywords:
  - Oracle Wallet Spring Boot
  - Oracle AI Database mTLS
  - Autonomous Database wallet
sidebar_position: 2
---

# Oracle AI Database Wallet

The Wallet starter adds the Oracle security libraries needed for Oracle AI Database Wallet based authentication, including common Autonomous AI Database mTLS scenarios.

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.database.spring</groupId>
  <artifactId>oracle-spring-boot-starter-wallet</artifactId>
</dependency>
```

Use this starter together with the UCP starter or another Oracle JDBC-based integration when your connection relies on wallet credentials and TNS configuration.
