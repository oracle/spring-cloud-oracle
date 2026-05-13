/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

class GenAiModelSelectedCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return isOracleModelSelected(context, PropertyNames.CHAT_MODEL_PROPERTY)
                || isOracleModelSelected(context, PropertyNames.EMBEDDING_MODEL_PROPERTY);
    }

    private boolean isOracleModelSelected(ConditionContext context, String propertyName) {
        String selectedModel = context.getEnvironment().getProperty(propertyName);
        return selectedModel == null || PropertyNames.MODEL_VALUE.equalsIgnoreCase(selectedModel);
    }

}
