# TxEventQ support for Spring Cloud Stream

This version of the binder supports Spring Boot 3+/Spring framework 6+.

## Getting started

Install the project after cloning this repo.
```
> mvn clean install
```

If you want to skip running the tests build using the following command: 
```
> mvn clean install -DskipTests
```

To use in an application project, add the following dependencies apart from regular spring-cloud-stream project dependencies:
```
<dependency>
	<groupId>com.oracle.database.cstream</groupId>
	<artifactId>spring-cloud-stream-binder-oracle-txeventq</artifactId>
	<version>0.9.0</version>
</dependency>
```

For some specific features of the binder, it is required that ojdbc11 and ucp versions are at least 23.3.0.23.09. Both of these may be added independently in the project to ensure the latest versions. Alternatively, override Spring's oracle-database.version property inside the properties section of pom.xml.

```
<properties>
	<oracle-database.version>23.3.0.23.09</oracele-database.version>
</properties>
```


### Steps to Create an Oracle Wallet

In order to connect to an Oracle database an Oracle Wallet will need to be created and specific properties will need to be set in the properties or yml file for your application. The
description below will provide information about what is required to create an Oracle Wallet.

Create or modify a tnsnames.ora file. The entry in the file should have the following form.

```text
alias=(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=host)(PORT=port))(CONNECT_DATA=(SERVICE_NAME=service)))
```

An Oracle Wallet will also need to be created in order for the Connector to connect to the database.
Refer to the following site for additional details on how to create an Oracle Wallet [JDBC Thin Connections with a Wallet](https://docs.oracle.com/en/cloud/paas/autonomous-database/adbsa/connect-jdbc-thin-wallet.html#GUID-BE543CFD-6FB4-4C5B-A2EA-9638EC30900D)
and [orapki Utility](https://docs.oracle.com/cd/B19306_01/network.102/b14268/asoappf.htm#CDEFHBGA).

Oracle recommends creating and managing the Wallet in a database environment since this environment provides all the necessary commands and libraries,
including the $ORACLE_HOME/oracle_common/bin/mkstore command.

Enter the following command to create a wallet:

```bash
mkstore -wrl <wallet_location> -create
```

The mkstore command above will prompt for a password that will be used for subsequent commands. Passwords must have a minimum length of eight characters and contain alphabetic characters combined with numbers or special characters.
If the create is successful when you go to the wallet location specified above a couple of cwallet and ewallet files should have been created in that directory.

Enter the following command to add the credential for the data source name added previously to tnsnames.ora to the Oracle Wallet:

```bash
mkstore -wrl <wallet_location> -createCredential <alias name from tnsnames.ora> <username> <password>
```

If a password is requested enter in the password from the step above.

The wallet directory that will need to be specified in the connection properties file below should contain the following files.

-   cwallet.sso
-   ewallet.p12

### Setup the Application Properties File For Connecting to Oracle Database

In your application.properties file add the following properties to connect to the required database.

```text
# Indicate the directory location of where the Oracle wallet is placed i.e. C:/tmp/wallet.
# The cwallet.sso and ewallet.p12 files should be placed into this directory.
# Oracle Wallet provides a simple and easy method to manage database credentials across multiple domains.
# We will be using the Oracle TNS (Transport Network Substrate) administrative file to hide the details
# of the database connection string (host name, port number, and service name) from the datasource definition
# and instead use an alias.
spring.datasource.oracleucp.connection-properties.oracle.net.wallet_location=C:/tmp/wallet

# Indicate the directory location of the where the tnsnames.ora location is located i.e C:/tmp/tnsnames.
# The entry in the tnsnames.ora should have the following format:
# <aliasname> = (DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = TCP)(Host = <hostname>)(Port = <port>)))
#(CONNECT_DATA =(SERVICE_NAME = <service_name>)))
spring.datasource.oracleucp.connection-properties.oracle.net.tns_admin=C:/tmp/wallet

# The TNS alias name for the database to connect to stored in the tnsnames.ora.
# An Oracle Wallet must be created and will be used to connect to the database.
spring.datasource.url=jdbc:oracle:thin:@cdb1_pdb1_wallet

# Additional configuration for PoolDataSource
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.datasource.type=oracle.ucp.jdbc.PoolDataSource
spring.datasource.oracleucp.connection-factory-class-name=oracle.jdbc.pool.OracleDataSource
```

One sample configuration can be: 
```
spring.datasource.url=jdbc:oracle:thin:@cdb1_pdb1_wallet
spring.datasource.oracleucp.connection-properties.oracle.net.wallet_location=C:/tmp/wallet
spring.datasource.oracleucp.connection-properties.oracle.net.tns_admin=C:/tmp/wallet
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.datasource.type=oracle.ucp.jdbc.PoolDataSource
spring.datasource.oracleucp.connection-factory-class-name=oracle.jdbc.pool.OracleDataSource
```

This configured PoolDataSource will be used automatically by the binder for connecting to the database. 

You may now create Consumer and Producer beans along with their configuration properties for your application: 

### TeqTestAppApplication.java
```
package com.example.teqtestapp;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TeqTestAppApplication {

	static int y = 0;

	public static void main(String[] args) {
		SpringApplication.run(TeqTestAppApplication.class, args);
	}

	@Bean
	public Consumer<TestObject> myconsumer() {
		return to -> System.out.println("Received: " + to);
	}
	
	@Bean
	public Supplier<TestObject> myproducer() {
		return () -> {
			System.out.println("Sending Message: ");
			return new TestObject(y++);
		};
	}
}

class TestObject {
	private int x;
	
	TestObject(int x) {
		this.x = x;
	}
	
	TestObject() {
		
	}
	
	public int getX() {
		return this.x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public String toString() {
		return "TestObject[x=" + this.x + "]";
	}
}
```

### application.properties
```
spring.datasource.url=jdbc:oracle:thin:@cdb1_pdb1_wallet
spring.datasource.oracleucp.connection-properties.oracle.net.wallet_location=C:/tmp/wallet
spring.datasource.oracleucp.connection-properties.oracle.net.tns_admin=C:/tmp/wallet
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.datasource.type=oracle.ucp.jdbc.PoolDataSource
spring.datasource.oracleucp.connection-factory-class-name=oracle.jdbc.pool.OracleDataSource

spring.cloud.function.definition=myconsumer;myproducer
spring.cloud.stream.bindings.myconsumer-in-0.destination=AADI_TEST
spring.cloud.stream.bindings.myconsumer-in-0.group=t1
spring.cloud.stream.bindings.myproducer-out-0.destination=AADI_TEST
spring.cloud.stream.bindings.myproducer-out-0.producer.requiredGroups=t1
```

Note: If you want partitioning features (deq from a particular shard), please use Oracle database version 23.4
