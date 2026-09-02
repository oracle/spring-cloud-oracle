# Copyright (c) 2026, Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

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

test: test_starters test_cloud_oci test_stream_binder test_spring_ai

test_starters:
	$(MAKE) -C database/starters test

test_cloud_oci:
	$(MAKE) -C spring-cloud-oci test

test_stream_binder:
	$(MAKE) -C database/spring-cloud-stream-binder-oracle-txeventq test

test_spring_ai:
	$(MAKE) -C spring-ai-oracle test

spotbugs: spotbugs_starters spotbugs_cloud_oci spotbugs_stream_binder spotbugs_spring_ai

spotbugs_starters:
	$(MAKE) -C database/starters spotbugs

spotbugs_cloud_oci:
	$(MAKE) -C spring-cloud-oci spotbugs

spotbugs_stream_binder:
	$(MAKE) -C database/spring-cloud-stream-binder-oracle-txeventq spotbugs

spotbugs_spring_ai:
	$(MAKE) -C spring-ai-oracle spotbugs

install: install_starters install_cloud_oci install_stream_binder install_spring_ai

install_starters:
	$(MAKE) -C database/starters install

install_cloud_oci:
	$(MAKE) -C spring-cloud-oci install

install_stream_binder: install_starters
	$(MAKE) -C database/spring-cloud-stream-binder-oracle-txeventq install

install_spring_ai:
	$(MAKE) -C spring-ai-oracle install

.PHONY: sca_starters sca_cloud_oci sca_stream_binder sca_spring_ai sca \
	test_starters test_cloud_oci test_stream_binder test_spring_ai test \
	spotbugs_starters spotbugs_cloud_oci spotbugs_stream_binder spotbugs_spring_ai spotbugs \
	install_starters install_cloud_oci install_stream_binder install_spring_ai install
