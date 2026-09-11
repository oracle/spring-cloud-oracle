# Copyright (c) 2026, Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/.

FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY . .

RUN mvn --batch-mode --no-transfer-progress \
    -pl oracle-spring-boot-starter-samples/oracle-spring-boot-sample-ucp-jpa \
    -am \
    -DskipTests \
    clean package

RUN find oracle-spring-boot-starter-samples/oracle-spring-boot-sample-ucp-jpa/target \
    -maxdepth 1 \
    -type f \
    -name 'oracle-spring-boot-sample-ucp-jpa-*.jar' \
    ! -name '*-sources.jar' \
    ! -name '*-javadoc.jar' \
    -exec cp '{}' /workspace/app.jar \; \
    -quit

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/app.jar app.jar

EXPOSE 9001 9002
USER 1001

ENTRYPOINT ["java", "-jar", "app.jar"]
