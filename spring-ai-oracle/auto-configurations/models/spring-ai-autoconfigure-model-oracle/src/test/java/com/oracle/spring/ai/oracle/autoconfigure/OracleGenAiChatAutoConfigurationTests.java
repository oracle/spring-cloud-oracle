/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import java.io.InputStream;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.requests.ApplyGuardrailsRequest;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.requests.EmbedTextRequest;
import com.oracle.bmc.generativeaiinference.requests.GenerateTextRequest;
import com.oracle.bmc.generativeaiinference.requests.RerankTextRequest;
import com.oracle.bmc.generativeaiinference.requests.SummarizeTextRequest;
import com.oracle.bmc.generativeaiinference.responses.ApplyGuardrailsResponse;
import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;
import com.oracle.bmc.generativeaiinference.responses.GenerateTextResponse;
import com.oracle.bmc.generativeaiinference.responses.RerankTextResponse;
import com.oracle.bmc.generativeaiinference.responses.SummarizeTextResponse;
import com.oracle.spring.ai.oracle.OracleGenAiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class OracleGenAiChatAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OracleGenAiChatAutoConfiguration.class))
            .withUserConfiguration(TestOciBeans.class)
            .withPropertyValues(
                    "spring.ai.model.chat=oracle",
                    "spring.ai.oracle.chat.options.compartment-id=compartment",
                    "spring.ai.oracle.chat.options.model=model");

    @Test
    void createsChatModelWhenOracleIsSelected() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ChatModel.class);
            assertThat(context).hasSingleBean(OracleGenAiChatModel.class);
        });
    }

    @Test
    void doesNotCreateChatModelWhenOracleIsNotSelected() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(OracleGenAiChatAutoConfiguration.class))
                .withUserConfiguration(TestOciBeans.class)
                .run(context -> assertThat(context).doesNotHaveBean(ChatModel.class));
    }

    @Test
    void backsOffWhenUserProvidesChatModel() {
        contextRunner.withUserConfiguration(UserChatModelConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(ChatModel.class);
                    assertThat(context).doesNotHaveBean(OracleGenAiChatModel.class);
                });
    }

    @Test
    void bindsChatOptions() {
        contextRunner
                .withPropertyValues(
                        "spring.ai.oracle.chat.options.temperature=0.4",
                        "spring.ai.oracle.chat.options.max-tokens=12",
                        "spring.ai.oracle.chat.options.api-format=COHERE_V2")
                .run(context -> {
                    OracleGenAiChatProperties properties = context.getBean(OracleGenAiChatProperties.class);
                    assertThat(properties.getOptions().getTemperature()).isEqualTo(0.4);
                    assertThat(properties.getOptions().getMaxTokens()).isEqualTo(12);
                    assertThat(properties.getOptions().getApiFormat().name()).isEqualTo("COHERE_V2");
                });
    }

    @Test
    void backsOffWhenUserProvidesAuthProvider() {
        contextRunner.run(context ->
                assertThat(context.getBean(BasicAuthenticationDetailsProvider.class))
                        .isSameAs(context.getBean(TestOciBeans.class).authenticationDetailsProvider()));
    }

    @Configuration(proxyBeanMethods = false)
    static class TestOciBeans {

        private final BasicAuthenticationDetailsProvider authenticationDetailsProvider =
                new TestAuthenticationDetailsProvider();

        private final GenerativeAiInference generativeAiInference = new NoOpGenerativeAiInference();

        @Bean
        BasicAuthenticationDetailsProvider authenticationDetailsProvider() {
            return authenticationDetailsProvider;
        }

        @Bean
        GenerativeAiInference generativeAiInference() {
            return generativeAiInference;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class UserChatModelConfiguration {

        @Bean
        ChatModel chatModel() {
            return prompt -> null;
        }
    }

    private static final class TestAuthenticationDetailsProvider implements BasicAuthenticationDetailsProvider {

        @Override
        public String getKeyId() {
            return null;
        }

        @Override
        public InputStream getPrivateKey() {
            return null;
        }

        @Override
        public String getPassPhrase() {
            return null;
        }

        @Override
        public char[] getPassphraseCharacters() {
            return new char[0];
        }
    }

    private static final class NoOpGenerativeAiInference implements GenerativeAiInference {

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
        public ApplyGuardrailsResponse applyGuardrails(ApplyGuardrailsRequest request) {
            throw new UnsupportedOperationException("No OCI calls are expected in auto-configuration tests.");
        }

        @Override
        public com.oracle.bmc.generativeaiinference.responses.ChatResponse chat(ChatRequest request) {
            throw new UnsupportedOperationException("No OCI calls are expected in auto-configuration tests.");
        }

        @Override
        public EmbedTextResponse embedText(EmbedTextRequest request) {
            throw new UnsupportedOperationException("No OCI calls are expected in auto-configuration tests.");
        }

        @Override
        public GenerateTextResponse generateText(GenerateTextRequest request) {
            throw new UnsupportedOperationException("No OCI calls are expected in auto-configuration tests.");
        }

        @Override
        public RerankTextResponse rerankText(RerankTextRequest request) {
            throw new UnsupportedOperationException("No OCI calls are expected in auto-configuration tests.");
        }

        @Override
        public SummarizeTextResponse summarizeText(SummarizeTextRequest request) {
            throw new UnsupportedOperationException("No OCI calls are expected in auto-configuration tests.");
        }

        @Override
        public void close() {
        }
    }
}
