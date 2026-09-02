# ----------------------------------------------------------------------------
#  Copyright (c) 2026, Oracle and/or its affiliates.
#  Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
# ----------------------------------------------------------------------------

.PHONY: test spotbugs install

mvncmd=mvnd

ifeq (, $(shell which ${mvncmd}))
mvncmd=mvn
endif

test:
	${mvncmd} test

spotbugs:
	${mvncmd} package -Pspotbugs -DskipTests -Dcyclonedx.skip=true com.github.spotbugs:spotbugs-maven-plugin:check

install:
	${mvncmd} install -DskipTests -Dcyclonedx.skip=true -Dspotbugs.skip=true
