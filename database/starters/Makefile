# ----------------------------------------------------------------------------
#  Copyright (c) 2025, 2026, Oracle and/or its affiliates.
#  Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
# ----------------------------------------------------------------------------

.PHONY: clean build docs javadocs test spotbugs install

mvncmd=mvnd

ifeq (, $(shell which ${mvncmd}))
mvncmd=mvn
endif


build:
	${mvncmd} verify

test:
	${mvncmd} test

spotbugs:
	${mvncmd} package -Pspotbugs -DskipTests -Dcyclonedx.skip=true -Dspotbugs.xmlOutput=true com.github.spotbugs:spotbugs-maven-plugin:4.10.4.0:spotbugs
	${mvncmd} -Pspotbugs -DskipTests -Dcyclonedx.skip=true -Dspotbugs.htmlOutput=true -DoutputEncoding=UTF-8 com.github.spotbugs:spotbugs-maven-plugin:4.10.4.0:spotbugs-aggregate

install:
	${mvncmd} install -DskipTests -Dcyclonedx.skip=true -Dspotbugs.skip=true

clean:
	${mvncmd} clean

docs:
	${mvncmd} verify -Pasciidocs -DskipTests=true

javadocs:
	${mvncmd} clean package javadoc:aggregate -DskipTests=true -e
