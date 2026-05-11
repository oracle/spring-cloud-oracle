/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import com.oracle.spring.ai.oracle.OracleGenAiChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for OCI Generative AI chat.
 */
@ConfigurationProperties(prefix = OracleGenAiChatProperties.CONFIG_PREFIX)
public class OracleGenAiChatProperties {

    public static final String CONFIG_PREFIX = "spring.ai.oracle.chat";

    private OracleGenAiChatOptions options = new OracleGenAiChatOptions();

    public OracleGenAiChatOptions getOptions() {
        return options;
    }

    public void setOptions(OracleGenAiChatOptions options) {
        this.options = options;
    }
}
