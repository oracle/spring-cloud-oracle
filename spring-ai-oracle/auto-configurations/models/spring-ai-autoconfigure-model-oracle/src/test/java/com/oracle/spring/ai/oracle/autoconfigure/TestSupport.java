/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.generativeai.GenerativeAi;
import com.oracle.bmc.generativeai.GenerativeAiClient;
import com.oracle.bmc.generativeai.model.Model;
import com.oracle.bmc.generativeai.model.ModelCapability;
import com.oracle.bmc.generativeai.model.ModelSummary;
import com.oracle.bmc.generativeai.model.SortOrder;
import com.oracle.bmc.generativeai.requests.ListModelsRequest;
import com.oracle.bmc.generativeai.responses.ListModelsResponse;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.util.StringUtils.hasText;

/**
 * Shared OCI Generative AI live-test setup and model discovery helpers.
 */
final class TestSupport {

    static final String COMPARTMENT_ID_ENV = "OCI_COMPARTMENT_ID";

    static final String CONFIG_FILE_ENV = "OCI_CONFIG_FILE";

    static final String PROFILE_ENV = "OCI_PROFILE";

    static final String REGION_ENV = "OCI_REGION";

    private static final String CONFIG_FILE_PROPERTY = PropertyNames.CONFIG_PREFIX + ".config-file";

    private static final String PROFILE_PROPERTY = PropertyNames.CONFIG_PREFIX + ".profile";

    private static final String REGION_PROPERTY = PropertyNames.CONFIG_PREFIX + ".region";

    private TestSupport() {
    }

    static String requiredCompartmentId(String testDescription) {
        String compartmentId = setting(COMPARTMENT_ID_ENV);
        assumeTrue(hasText(compartmentId), "Set " + COMPARTMENT_ID_ENV + " to run " + testDescription + ".");
        return compartmentId;
    }

    static AuthenticationProperties authenticationProperties() {
        AuthenticationProperties properties = new AuthenticationProperties();
        properties.setAuthenticationType(AuthenticationProperties.Type.FILE);
        properties.setConfigFile(setting(CONFIG_FILE_ENV));
        properties.setProfile(setting(PROFILE_ENV));
        properties.setRegion(setting(REGION_ENV));
        return properties;
    }

    static <T extends ModelCandidate> List<T> loadSelectedModels(
            BasicAuthenticationDetailsProvider authenticationDetailsProvider,
            AuthenticationProperties authProperties, String compartmentId, ModelCapability capability,
            String configuredModelEnv, Function<ModelSummary, T> candidateFactory, String modelDescription)
            throws Exception {
        String configuredModel = setting(configuredModelEnv);
        Map<String, T> latestProviderModels = new LinkedHashMap<>();
        try (GenerativeAi client = GenerativeAiClient.builder().build(authenticationDetailsProvider)) {
            configureRegion(client, authenticationDetailsProvider, authProperties);

            String page = null;
            do {
                ListModelsResponse response = client.listModels(ListModelsRequest.builder()
                        .compartmentId(compartmentId)
                        .capability(capability)
                        .lifecycleState(Model.LifecycleState.Active)
                        .sortBy(ListModelsRequest.SortBy.TimeCreated)
                        .sortOrder(SortOrder.Desc)
                        .page(page)
                        .build());
                if (response.getModelCollection() != null && response.getModelCollection().getItems() != null) {
                    for (ModelSummary model : response.getModelCollection().getItems()) {
                        T candidate = candidateFactory.apply(model);
                        if (candidate == null) {
                            continue;
                        }
                        if (hasText(configuredModel) && candidate.modelId().equals(configuredModel)) {
                            return List.of(candidate);
                        }
                        latestProviderModels.putIfAbsent(candidate.provider(), candidate);
                    }
                }
                page = response.getOpcNextPage();
            }
            while (hasText(page));
        }
        if (hasText(configuredModel)) {
            assumeTrue(false, "Configured OCI Generative AI " + modelDescription + " model " + configuredModel
                    + " was not returned by the live model list call.");
        }
        return List.copyOf(latestProviderModels.values());
    }

    static boolean supportsCapability(ModelSummary model, ModelCapability capability) {
        return model.getCapabilities() != null && model.getCapabilities().contains(capability);
    }

    static boolean isAvailableOnDemand(ModelSummary model) {
        return model.getTimeDeprecated() == null && model.getTimeOnDemandRetired() == null;
    }

    static String inferenceModelId(ModelSummary model) {
        if (hasText(model.getDisplayName())) {
            return model.getDisplayName();
        }
        return model.getId();
    }

    static String modelProvider(String modelId, ModelSummary model) {
        if (hasText(model.getVendor())) {
            return model.getVendor().trim().toLowerCase(java.util.Locale.ROOT);
        }
        int separator = modelId.indexOf('.');
        if (separator > 0) {
            return modelId.substring(0, separator).toLowerCase(java.util.Locale.ROOT);
        }
        return modelId.toLowerCase(java.util.Locale.ROOT);
    }

    static void addAuthenticationProperties(List<String> properties) {
        properties.add(PropertyNames.CONFIG_PREFIX + ".authentication-type=FILE");
        addIfPresent(properties, CONFIG_FILE_PROPERTY, setting(CONFIG_FILE_ENV));
        addIfPresent(properties, PROFILE_PROPERTY, setting(PROFILE_ENV));
        addIfPresent(properties, REGION_PROPERTY, setting(REGION_ENV));
    }

    static String setting(String environmentVariable) {
        return System.getenv(environmentVariable);
    }

    private static void configureRegion(GenerativeAi client,
            BasicAuthenticationDetailsProvider authenticationDetailsProvider,
            AuthenticationProperties authProperties) {
        if (hasText(authProperties.getRegion())) {
            client.setRegion(authProperties.getRegion());
        }
        else if (authenticationDetailsProvider instanceof RegionProvider regionProvider
                && regionProvider.getRegion() != null) {
            client.setRegion(regionProvider.getRegion());
        }
    }

    private static void addIfPresent(List<String> properties, String name, String value) {
        if (hasText(value)) {
            properties.add(name + "=" + value);
        }
    }

    interface ModelCandidate {
        String provider();

        String modelId();
    }
}
