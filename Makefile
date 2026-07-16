SCA_TOOL ?= $(TOOL_PATH)
TOOL_VERSION ?= 3.6.21
MVN ?= mvn
AGGREGATE_COMMAND ?= $(MVN) oracle.$(SCA_TOOL):$(SCA_TOOL)-maven-plugin:$(TOOL_VERSION):aggregate-translate \
                         oracle.$(SCA_TOOL):$(SCA_TOOL)-maven-plugin:$(TOOL_VERSION):aggregate-analyze -P $(SCA_TOOL)

sca: sca_starters sca_cloud_oci sca_stream_binder sca_spring_ai

sca_starters:
	$(AGGREGATE_COMMAND) -f database/starters/pom.xml

sca_cloud_oci:
	$(AGGREGATE_COMMAND) -f spring-cloud-oci/pom.xml

sca_stream_binder:
	$(AGGREGATE_COMMAND) -f database/spring-cloud-stream-binder-oracle-txeventq/pom.xml

sca_spring_ai:
	$(AGGREGATE_COMMAND) -f spring-ai-oracle/pom.xml

.PHONY: sca_starters sca_cloud_oci sca_stream_binder sca_spring_ai sca
