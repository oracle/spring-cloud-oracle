/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

interface PropertyNames {

    String MODEL_VALUE = "oci-genai";

    String CHAT_MODEL_PROPERTY = "spring.ai.model.chat";

    String EMBEDDING_MODEL_PROPERTY = "spring.ai.model.embedding";

    String CONFIG_PREFIX = "spring.ai.oci.genai";

    String CHAT_CONFIG_PREFIX = CONFIG_PREFIX + ".chat";

    String EMBEDDING_CONFIG_PREFIX = CONFIG_PREFIX + ".embedding";
}
