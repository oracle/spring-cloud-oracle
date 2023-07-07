#!/bin/bash -x

# Copyright (c) 2023, Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

# This command regenerates the docs after editing.
# You'll have to run this after editing the src/main/asciidoc/README.adoc

../mvnw clean install -Pdocs
