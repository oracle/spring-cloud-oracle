# Hikari to UCP Open Rewrite Recipe

Current Version is `0.0.1-SNAPSHOT` and is under development. Please file GitHub issues for issues, enhancements and more.

## Hikari to UCP rewrite Recipe

> **_NOTE:_**  In the pre release the rewrite works best with `properties` files. `YAML` files works too but the formatting of the outcome isn't pretty.

This recipe will change the Hikare connection pool parameters and dependency from Hikari to Oracle Universal Connection Pooling (UCP). The [UCP documentation](https://docs.oracle.com/en/database/oracle/oracle-database/23/jjucp/index.html).

The following properties are rewritten:

### Maven dependencies `pom.xml`

The Hikari dependency is removed and replaced with SPring Boot starters for Oracle UCP and Oracle Wallet (commonly used to connect to an autonomous database (ADB)).

```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
</dependency>
```

is changed to:

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-ucp</artifactId>
    <version>25.1.0</version>
</dependency>
```

The following dependency is removed (all ojdbc* variants) and replaced by the UCP Starter:

```xml
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc*</artifactId>
</dependency>
```

And the following dependency is added, as it is commonly used to connect to an autonomous database (ADB):

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-wallet</artifactId>
    <version>25.1.0</version>
</dependency>
```

### Spring Boot Connection Pooling configuration `application.properties`

> **_NOTE:_**  The recipe will change Hikari's milliseconds to seconds.

The following properties are rewritten. [UCP documentation](https://docs.oracle.com/en/database/oracle/oracle-database/23/jjucp/index.html)

| Hikari Property | Oracle UCP Property | Notes |
|-----------------|---------------------|-------|
| N/A | `spring.datasource.driver-class-name` | Will be set to `oracle.jdbc.OracleDriver`       |
| N/A | `spring.datasource.type` | Will be set to `oracle.ucp.jdbc.PoolDataSource` |
| `spring.datasource.hikari.pool-name` | `spring.datasource.oracleucp.connection-pool-name` | |
| `spring.datasource.hikari.maximum-pool-size` | `spring.datasource.oracleucp.max-pool-size` | |
| `spring.datasource.hikari.minimum-idle` | `spring.datasource.oracleucp.min-pool-size` | |
| `spring.datasource.hikari.connection-timeout` | `spring.datasource.oracleucp.connection-wait-timeout` | |
| `spring.datasource.hikari.idle-timeout` | `spring.datasource.oracleucp.inactive-connection-timeout` | |
| `spring.datasource.hikari.connection-test-query` | `spring.datasource.oracleucp.s-q-l-for-validate-connection` | |
| `spring.datasource.hikari.max-lifetime` | `spring.datasource.oracleucp.max-connection-reuse-time` | |
| `spring.datasource.hikari.validation-timeout` | `spring.datasource.oracleucp.connection-validation-timeout` | |

The following UCP properties are added:

| Oracle UCP Property | Value | Notes |
|---------------------|-------|-------|
| `spring.datasource.oracleucp.initial-pool-size` | 5 | [UCP documentation](https://docs.oracle.com/en/database/oracle/oracle-database/23/jjucp/index.html) |

The following Hikari Properties do not have identical UCP properties and will be commented out in the rewritten properties file. [UCP documentation](https://docs.oracle.com/en/database/oracle/oracle-database/23/jjucp/index.html)

| Hikari UCP Property | Notes |
|---------------------|-------|
| `spring.datasource.hikari.auto-commit` | Use Oracle JDBC driver connection property autoCommit |
| `spring.datasource.hikari.register-mbeans` | UCP always attempts registration |
| `spring.datasource.hikari.thread-factory` | UCP supports setTaskManager instead |
| `spring.datasource.hikari.scheduled-executor` | UCP supports setTaskManager instead |
| `spring.datasource.hikari.keepalive-time` | Closest is to use driver connection properties oracle.net.keepAlive + oracle.net.TCP_KEEPIDLE |

## Build the Open Rewrite Recipe

The repo is using maven. To build the recipe run the following command:

```shell
mvn install
```

## To use the Recipe

 In the repository you want to test your recipe against, update the `pom.xml` to include the following:

 ```xml
 <project>
    <build>
        <plugins>
            <plugin>
                <groupId>org.openrewrite.maven</groupId>
                <artifactId>rewrite-maven-plugin</artifactId>
                <version>6.3.2</version> <!-- Verify the version, March 2025 -->
                <configuration>
                    <activeRecipes>
                        <recipe>ConvertHikariToUCP </recipe>
                    </activeRecipes>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>com.oracle.cloud.recipes</groupId>
                        <artifactId>hikariucp</artifactId>
                        <version>0.0.1-SNAPSHOT</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
</project>
```
