# Copyright (c) 2026, Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/.

# Oracle OpenJDK build image from Oracle Container Registry.
FROM container-registry.oracle.com/java/openjdk:21 AS build

ARG MAVEN_VERSION=3.9.11
ARG MAVEN_SHA512=bcfe4fe305c962ace56ac7b5fc7a08b87d5abd8b7e89027ab251069faebee516b0ded8961445d6d91ec1985dfe30f8153268843c89aa392733d1a3ec956c9978

RUN curl --fail --silent --show-error --location \
        --output /tmp/apache-maven.tar.gz \
        "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
    && echo "${MAVEN_SHA512}  /tmp/apache-maven.tar.gz" | sha512sum --check - \
    && tar --extract --gzip --file /tmp/apache-maven.tar.gz --directory /opt \
    && ln --symbolic "/opt/apache-maven-${MAVEN_VERSION}/bin/mvn" /usr/local/bin/mvn \
    && rm /tmp/apache-maven.tar.gz

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

# Oracle OpenJDK runtime from Oracle Container Registry.
FROM container-registry.oracle.com/java/openjdk:21

WORKDIR /app
COPY --from=build /workspace/app.jar app.jar

EXPOSE 9001 9002
USER 1001

ENTRYPOINT ["java", "-jar", "app.jar"]
