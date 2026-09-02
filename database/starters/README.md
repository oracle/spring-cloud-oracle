# Oracle Database Spring Starters

Oracle Database Spring Starters implements Spring Boot Starters to facilitate the use of Oracle Database with Spring Boot.

The following starters are provided:

| Starter                                                                                                                                  | Description                                                                     |
|------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| [Oracle Spring Boot Starter UCP](oracle-spring-boot-starter-ucp)                                                                         | Autoconfigure UCP for Oracle Database, over the default Hikari Connection Pool. |
| [Oracle Spring Boot UCP Micrometer](oracle-spring-boot-ucp-micrometer)                                                                  | Export UCP runtime pool statistics through Micrometer.                            |
| [Oracle Spring Boot Starter AQJMS](oracle-spring-boot-starter-aqjms)                                                                     | Autoconfigure Oracle Database AQJMS Connections.                                |
| [Oracle Spring Boot Starter Wallet](oracle-spring-boot-starter-wallet)                                                                   | Bundle dependencies for Oracle Wallet.                                          |
| [Oracle Spring Boot Starter JSON Collections](oracle-spring-boot-starter-json-collections)                                               | Autoconfiguration and utilities for JSON with Oracle Database                   |
| [Oracle Spring Boot Starter Spatial](oracle-spring-boot-starter-spatial)                                                                 | Autoconfiguration and helper utilities for Oracle Spatial with GeoJSON-first APIs |
| [Oracle Spring Boot Starter for the Kafka Java Client for Oracle Database Transactional Event Queues](oracle-spring-boot-starter-okafka) | Autoconfiguration for Kafka Java Client for Oracle Transactional Event Queues   |

## Build quality checks

Run the test suite with `make test` and SpotBugs plus FindSecBugs security analysis with `make spotbugs`, which enables the opt-in Maven `spotbugs` profile. Each analyzed Maven module writes an HTML report to `target/site/spotbugs.html` and an XML report to `target/spotbugsXml.xml`; the target then combines those XML results into an aggregate HTML report at `target/site/spotbugs.html` in this multi-module project. Use `make install` to install artifacts while skipping tests, CycloneDX generation, and SpotBugs. SpotBugs reports existing findings without failing the build.
