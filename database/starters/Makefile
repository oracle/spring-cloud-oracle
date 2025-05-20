# ----------------------------------------------------------------------------
#  Copyright (c) 2025, Oracle and/or its affiliates.
#  Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
# ----------------------------------------------------------------------------

.PHONY: clean build docs javadocs

mvncmd=mvnd

ifeq (, $(shell which ${mvncmd}))
mvncmd=mvn
endif


build:
	${mvncmd} verify

clean:
	${mvncmd} clean

docs:
	${mvncmd} verify -Pasciidocs -DskipTests=true

javadocs:
	${mvncmd} clean package javadoc:aggregate -DskipTests=true -e
