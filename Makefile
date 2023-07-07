# ----------------------------------------------------------------------------
#  Copyright (c) 2023, Oracle and/or its affiliates.
#  Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
# ----------------------------------------------------------------------------

.PHONY: format build docs

format:
	mvnd spotless:apply

build:
	mvnd verify -DskipTests

clean:
	mvnd clean

docs:
	mvnd verify -Pdocs -DskipTests=true

javadocs:
	mvnd clean package javadoc:aggregate -DskipTests=true -e
