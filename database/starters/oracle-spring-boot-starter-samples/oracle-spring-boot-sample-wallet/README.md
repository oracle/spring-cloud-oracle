# Oracle Spring Boot Sample UCP using Wallet

This sample application demonstrates how to connect to an Autonomous database using the Oracle Spring Boot Starter UCP, [UCP Documentation](https://docs.oracle.com/en/database/oracle/oracle-database/23/jjucp/), and Oracle Spring Boot Starter Wallet, [Database Connection with Wallet](https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/adbsb/connect-download-wallet.html), all while using the best connection pooling library for Oracle Database with Spring Boot.

## Wallet

1. Download the Wallet using the following [instructions](https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/adbsb/connect-download-wallet.html#GUID-DED75E69-C303-409D-9128-5E10ADD47A35).
1. Unzip the Wallet ZIP file
1. Open the `sqlnet.ora` file and modify the line `WALLET_LOCATION = (SOURCE = (METHOD = file) (METHOD_DATA = (DIRECTORY="<DIRECTORY_OF_UNZIPPED_WALLET>")))` to make the `DIRECTORY` parameter point to the directory where you unzipped the wallet.

## Configure the application

 Open the `application.yaml` file and modify the `url` and `password` parameter to reflect your environment.

  ```yaml
  spring:
    application:
      name: ucp-wallet

    # Datasource Configuration
    datasource:
      url: jdbc:oracle:thin:@<TNSNAMES_ORA_ENTRY>?TNS_ADMIN=<DIRECTORY_OF_UNZIPPED_WALLET>
      username: ADMIN
      password: <PASSWORD>
      driver-class-name: oracle.jdbc.OracleDriver
      type: oracle.ucp.jdbc.PoolDataSource
      oracleucp:
        connection-factory-class-name: oracle.jdbc.pool.OracleDataSource
        connection-pool-name: UCPWalletConnectionPool
        initial-pool-size: 15
        min-pool-size: 10
        max-pool-size: 30
  ```

## Run the sample application

To run the testapplication, execute the following command:

```shell
mvn clean spring-boot:run
```

The output of the test application should look similar to this.

```text
Datasource is : oracle.ucp.jdbc.PoolDataSourceImpl 
Connection is : oracle.ucp.jdbc.proxy.oracle$1ucp$1jdbc$1proxy$1oracle$1ConnectionProxy$2oracle$1jdbc$1internal$1OracleConnection$$$Proxy@527fc8e
Hello World!
Oracle Database 23ai Enterprise Edition Release 23.0.0.0.0 - Production
Version 23.6.0.24.07
```

## Configure your project to use Oracle Spring Boot Starters for UCP and Wallet

To use Oracle Spring Boot Starters for UCP and Wallet from your Spring Boot application, add the following Maven dependency to your project:

```xml
<dependencies>
  <dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-ucp</artifactId>
  </dependency>
  <dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-wallet</artifactId>
  </dependency>
</dependencies>
```
