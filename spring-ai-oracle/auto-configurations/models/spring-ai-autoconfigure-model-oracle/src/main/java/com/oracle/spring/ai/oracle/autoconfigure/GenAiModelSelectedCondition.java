/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

class GenAiModelSelectedCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        boolean chatSelected = isOracleModelSelected(environment, PropertyNames.CHAT_MODEL_PROPERTY);
        boolean embeddingSelected = isOracleModelSelected(environment, PropertyNames.EMBEDDING_MODEL_PROPERTY);
        ConditionMessage.Builder message = ConditionMessage.forCondition("OCI Generative AI model selection");
        if (chatSelected || embeddingSelected) {
            return ConditionOutcome.match(message
                    .foundExactly(selectedMessage(environment, chatSelected, embeddingSelected)));
        }
        return ConditionOutcome.noMatch(message
                .because(PropertyNames.CHAT_MODEL_PROPERTY + " and " + PropertyNames.EMBEDDING_MODEL_PROPERTY
                        + " are both set to non-Oracle models"));
    }

    private boolean isOracleModelSelected(Environment environment, String propertyName) {
        String selectedModel = environment.getProperty(propertyName);
        return selectedModel == null || PropertyNames.MODEL_VALUE.equalsIgnoreCase(selectedModel);
    }

    private String selectedMessage(Environment environment, boolean chatSelected, boolean embeddingSelected) {
        if (chatSelected && embeddingSelected) {
            return PropertyNames.CHAT_MODEL_PROPERTY + "=" + selectedValue(environment, PropertyNames.CHAT_MODEL_PROPERTY)
                    + " and " + PropertyNames.EMBEDDING_MODEL_PROPERTY + "="
                    + selectedValue(environment, PropertyNames.EMBEDDING_MODEL_PROPERTY);
        }
        if (chatSelected) {
            return PropertyNames.CHAT_MODEL_PROPERTY + "=" + selectedValue(environment, PropertyNames.CHAT_MODEL_PROPERTY);
        }
        return PropertyNames.EMBEDDING_MODEL_PROPERTY + "="
                + selectedValue(environment, PropertyNames.EMBEDDING_MODEL_PROPERTY);
    }

    private String selectedValue(Environment environment, String propertyName) {
        String value = environment.getProperty(propertyName);
        return value != null ? value : "<missing>";
    }
}
