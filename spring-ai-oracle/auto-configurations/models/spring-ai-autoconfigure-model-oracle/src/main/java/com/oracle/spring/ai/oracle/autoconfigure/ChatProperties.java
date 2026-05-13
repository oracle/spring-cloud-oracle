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
@ConfigurationProperties(prefix = PropertyNames.CHAT_CONFIG_PREFIX)
public class ChatProperties extends OracleGenAiChatOptions {
}
