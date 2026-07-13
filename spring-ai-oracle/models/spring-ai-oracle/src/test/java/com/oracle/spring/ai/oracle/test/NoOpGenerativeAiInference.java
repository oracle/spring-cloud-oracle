/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.test;

import com.oracle.bmc.Region;
import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferencePaginators;
import com.oracle.bmc.generativeaiinference.requests.ApplyGuardrailsRequest;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.requests.EmbedTextRequest;
import com.oracle.bmc.generativeaiinference.requests.GenerateTextRequest;
import com.oracle.bmc.generativeaiinference.requests.ListGuardrailVersionsRequest;
import com.oracle.bmc.generativeaiinference.requests.RerankTextRequest;
import com.oracle.bmc.generativeaiinference.requests.SummarizeTextRequest;
import com.oracle.bmc.generativeaiinference.responses.ApplyGuardrailsResponse;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;
import com.oracle.bmc.generativeaiinference.responses.GenerateTextResponse;
import com.oracle.bmc.generativeaiinference.responses.ListGuardrailVersionsResponse;
import com.oracle.bmc.generativeaiinference.responses.RerankTextResponse;
import com.oracle.bmc.generativeaiinference.responses.SummarizeTextResponse;

public class NoOpGenerativeAiInference implements GenerativeAiInference {

    @Override
    public void refreshClient() {
    }

    @Override
    public void setEndpoint(String endpoint) {
    }

    @Override
    public String getEndpoint() {
        return null;
    }

    @Override
    public void setRegion(Region region) {
    }

    @Override
    public void setRegion(String regionId) {
    }

    @Override
    public void useRealmSpecificEndpointTemplate(boolean realmSpecificEndpointTemplateEnabled) {
    }

    @Override
    public void enableDualStackEndpoints(boolean b) {

    }

    @Override
    public ApplyGuardrailsResponse applyGuardrails(ApplyGuardrailsRequest request) {
        throw unexpectedOciCall();
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        throw unexpectedOciCall();
    }

    @Override
    public EmbedTextResponse embedText(EmbedTextRequest request) {
        throw unexpectedOciCall();
    }

    @Override
    public GenerateTextResponse generateText(GenerateTextRequest request) {
        throw unexpectedOciCall();
    }

    @Override
    public ListGuardrailVersionsResponse listGuardrailVersions(ListGuardrailVersionsRequest listGuardrailVersionsRequest) {
        return null;
    }

    @Override
    public RerankTextResponse rerankText(RerankTextRequest request) {
        throw unexpectedOciCall();
    }

    @Override
    public SummarizeTextResponse summarizeText(SummarizeTextRequest request) {
        throw unexpectedOciCall();
    }

    @Override
    public GenerativeAiInferencePaginators getPaginators() {
        return null;
    }

    @Override
    public void close() {
    }

    private static UnsupportedOperationException unexpectedOciCall() {
        return new UnsupportedOperationException("No OCI calls are expected in tests using NoOpGenerativeAiInference.");
    }
}
