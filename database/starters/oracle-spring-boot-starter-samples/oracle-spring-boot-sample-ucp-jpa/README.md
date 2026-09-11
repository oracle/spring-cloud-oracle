# Oracle Spring Boot Sample UCP JPA

This sample application demonstrates how to use the Oracle Spring Boot Starter UCP with Spring Data JPA, connecting to Oracle AI Database with powerful ORM abstractions that facilitate rapid development.

The Oracle Spring Boot Sample UCP JPA package includes a JPA entity, repository, and REST controller to interact with the JPA repository. All necessary configuration and dependencies are bootstrapped, with an end-to-end test demonstrating the functionality of Spring JPA with Oracle AI Database and UCP.

## Run the sample application

The sample application test uses Testcontainers to create a temporary Oracle AI Database Free container and requires a Docker runtime environment. The sample application demonstrates the use of Spring Data JPA with the Oracle Spring Boot Starter UCP.

To run the test application, run the following command:

```shell
mvn test
```

## Start Oracle AI Database Free and Grafana LGTM

The sample includes a Docker Compose environment for running Oracle AI Database Free and Grafana LGTM locally:

```shell
export ORACLE_PWD="${ORACLE_PWD:-Welcome12345}"
docker compose up --build -d
```

The default administrative password is `Welcome12345`. Change the `ORACLE_PWD` value before starting the services to use a different password. Compose builds and starts the sample application after the database initialization scripts finish. The services expose these host ports:

| Service | Port | Purpose |
| --- | --- | --- |
| Sample application | `9001` | Student REST API |
| Sample application | `9002` | Actuator API |
| Oracle AI Database Free | `1522` | Oracle listener |
| Grafana LGTM | `3000` | Grafana UI |
| Grafana LGTM | `4317` | OTLP over gRPC |
| Grafana LGTM | `4318` | OTLP over HTTP |

On first startup, the Compose database mounts [src/test/resources](./src/test/resources) into the official image's startup-script directory. It runs [01_testuser.sql](./src/test/resources/01_testuser.sql) to create the `TESTUSER` application user with password `testpwd`, then runs [02_student.sql](./src/test/resources/02_student.sql), which applies the shared [init.sql](./src/test/resources/sql/init.sql) schema to create the `STUDENT` table.

The application connects to `oracle-free` over the Compose network and exports Micrometer metrics to LGTM's OTLP HTTP receiver every five seconds. Start the continuous student workload to populate the dashboard with create, read, and delete activity:

```shell
./students-simulation.sh
```

Stop the simulation with `Ctrl+C`. Each cycle launches a concurrent batch of 50 varied requests by default, rotating through POST, collection GET, single-student GET, and DELETE operations. New students use varied data, deletes target different tracked students, and the planner keeps at least one record available for valid single-student reads instead of racing reads against deletes. Successful batches run back-to-back, while startup and failed-cycle retries wait one second by default. Override these settings with `STUDENT_API_URL`, `BURST_SIZE`, `REQUEST_INTERVAL_SECONDS`, and `MAX_STUDENTS` environment variables.

Open [Grafana](http://localhost:3000) and select **Dashboards > Oracle AI Database > Oracle UCP Metrics**. The Compose environment provisions the dashboard from [dashboards/ucp-metrics.json](./dashboards/ucp-metrics.json) and uses LGTM's built-in Prometheus data source. Use the pool selector to inspect one or more pools; the dashboard covers current state and capacity, utilization and pending requests, borrow/return throughput, acquisition outcomes and latency, connection inventory, and pool lifecycle signals.

The application also exposes its meter names at [http://localhost:9002/actuator/metrics](http://localhost:9002/actuator/metrics). To run the application directly with Maven instead, start only `oracle-free` and `grafana-lgtm`, then provide the database connection properties and enable OTLP metrics export with an endpoint of `http://localhost:4318/v1/metrics`.

### UCP connection-pool tuning

The default pool is intentionally small (2–4 connections) so the dashboard can show pressure under the concurrent workload. It checks idle and reused physical connections every 10 seconds, retires idle connections after 30 seconds, and reuses physical connections for no more than 60 seconds or 25 borrows. Borrowers wait up to two seconds when the pool is exhausted. It also enables abandoned-connection safeguards, harvesting thresholds for connections explicitly marked harvestable by application code, and a per-connection cache of 10 prepared or callable statements.

Stop the environment with:

```shell
docker compose down
```

## Configure Maven dependencies to use Oracle UCP and Spring Data JPA

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-ucp</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

## Configure application properties to use Oracle UCP as the datasource provider

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none
  datasource:
    username: ${USERNAME}
    password: ${PASSWORD}
    url: ${JDBC_URL}

    # Set these to use UCP over Hikari.
    driver-class-name: oracle.jdbc.OracleDriver
    type: oracle.ucp.jdbc.PoolDataSource
    oracleucp:
      # Starts two connections and maintains a 2–4 connection pool for the demo.
      initial-pool-size: 2
      min-pool-size: 2
      max-pool-size: 4
      connection-pool-name: UCPSampleApplication
      connection-factory-class-name: oracle.jdbc.pool.OracleDataSource
      # Closes available idle connections; borrowed connections are unaffected.
      inactive-connection-timeout: 30
      # Limits validation performed when a connection is borrowed.
      connection-validation-timeout: 5
      # Runs timeout enforcement every 10 seconds.
      timeout-check-interval: 10
      # Retires a physical connection after 60 seconds of lifetime.
      max-connection-reuse-time: 60
      # Retires a physical connection after 25 borrows.
      max-connection-reuse-count: 25
      # Waits up to two seconds when the pool is exhausted.
      connection-wait-duration: 2s
      # Reclaims a borrowed connection with no database activity for 10 seconds.
      abandoned-connection-timeout: 10
      # Reclaims a borrowed connection after 60 seconds, even if it is active.
      time-to-live-connection-timeout: 60
      # Applies a 15-second default statement execution timeout through UCP.
      query-timeout: 15
      # Begins harvesting when one connection is available.
      connection-harvest-trigger-count: 1
      # Reclaims up to two borrowed connections marked harvestable by callers.
      connection-harvest-max-count: 2
      # Caches up to 10 prepared or callable statements per physical connection.
      max-statements: 10
```

## Write a JPA repository and entity

```java
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, String> {}
```

```java
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "STUDENT")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    private String email;
    private String major;
    private double credits;
    private double gpa;
```

## UCP Metrics

The sample exposes the Actuator `metrics` endpoint on the configured management port, `9002`. Keep the `metrics` endpoint exposed in `application.yaml` to use these URLs.

With the application running, [http://localhost:9002/actuator/metrics](http://localhost:9002/actuator/metrics) lists the available meter names. To retrieve the connection-count measurements and their `idle` and `used` state tags, use [http://localhost:9002/actuator/metrics/db.client.connection.count](http://localhost:9002/actuator/metrics/db.client.connection.count).

The sample test verifies live UCP meters after Oracle AI Database activity. Portable pool-state meters use `db.client.connection.pool.name=UCPSampleApplication`; Oracle UCP vendor-extension meters continue to use `pool=UCPSampleApplication`.
