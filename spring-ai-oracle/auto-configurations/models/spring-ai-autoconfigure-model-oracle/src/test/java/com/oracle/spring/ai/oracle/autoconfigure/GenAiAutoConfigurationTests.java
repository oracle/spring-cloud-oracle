/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import java.io.InputStream;
import java.util.List;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.spring.ai.oracle.OracleGenAiChatModel;
import com.oracle.spring.ai.oracle.OracleGenAiEmbeddingModel;
import com.oracle.spring.ai.oracle.test.NoOpGenerativeAiInference;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class GenAiAutoConfigurationTests {

    private static final String CHAT_SELECTED = PropertyNames.CHAT_MODEL_PROPERTY + "="
            + PropertyNames.MODEL_VALUE;

    private static final String EMBEDDING_SELECTED = PropertyNames.EMBEDDING_MODEL_PROPERTY + "="
            + PropertyNames.MODEL_VALUE;

    private static final String CHAT_DISABLED = PropertyNames.CHAT_MODEL_PROPERTY + "=none";

    private static final String EMBEDDING_DISABLED = PropertyNames.EMBEDDING_MODEL_PROPERTY + "=none";

    private final ApplicationContextRunner chatContextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GenAiAutoConfiguration.class))
            .withUserConfiguration(TestOciBeans.class)
            .withPropertyValues(
                    CHAT_SELECTED,
                    PropertyNames.CHAT_CONFIG_PREFIX + ".compartment-id=compartment",
                    PropertyNames.CHAT_CONFIG_PREFIX + ".model=model");

    private final ApplicationContextRunner embeddingContextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GenAiAutoConfiguration.class))
            .withUserConfiguration(TestOciBeans.class)
            .withPropertyValues(
                    EMBEDDING_SELECTED,
                    PropertyNames.EMBEDDING_CONFIG_PREFIX + ".compartment-id=compartment",
                    PropertyNames.EMBEDDING_CONFIG_PREFIX + ".model=model");

    @Test
    void createsChatModelWhenOracleIsSelected() {
        chatContextRunner.run(context -> {
            assertThat(context).hasSingleBean(ChatModel.class);
            assertThat(context).hasSingleBean(OracleGenAiChatModel.class);
        });
    }

    @Test
    void createsChatModelWhenSelectorIsMissing() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GenAiAutoConfiguration.class))
                .withUserConfiguration(TestOciBeans.class)
                .withPropertyValues(
                        EMBEDDING_DISABLED,
                        PropertyNames.CHAT_CONFIG_PREFIX + ".compartment-id=compartment",
                        PropertyNames.CHAT_CONFIG_PREFIX + ".model=model")
                .run(context -> {
                    assertThat(context).hasSingleBean(ChatModel.class);
                    assertThat(context).hasSingleBean(OracleGenAiChatModel.class);
                });
    }

    @Test
    void doesNotCreateChatModelWhenOracleIsNotSelected() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GenAiAutoConfiguration.class))
                .withUserConfiguration(TestOciBeans.class)
                .withPropertyValues(
                        CHAT_DISABLED,
                        EMBEDDING_DISABLED)
                .run(context -> assertThat(context).doesNotHaveBean(ChatModel.class));
    }

    @Test
    void backsOffWhenUserProvidesChatModel() {
        chatContextRunner.withUserConfiguration(UserChatModelConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(ChatModel.class);
                    assertThat(context).doesNotHaveBean(OracleGenAiChatModel.class);
                });
    }

    @Test
    void bindsChatOptions() {
        chatContextRunner
                .withPropertyValues(
                        PropertyNames.CHAT_CONFIG_PREFIX + ".temperature=0.4",
                        PropertyNames.CHAT_CONFIG_PREFIX + ".max-tokens=12",
                        PropertyNames.CHAT_CONFIG_PREFIX + ".api-format=COHERE_V2")
                .run(context -> {
                    ChatProperties properties = context.getBean(ChatProperties.class);
                    assertThat(properties.getTemperature()).isEqualTo(0.4);
                    assertThat(properties.getMaxTokens()).isEqualTo(12);
                    assertThat(properties.getApiFormat().name()).isEqualTo("COHERE_V2");
                });
    }

    @Test
    void createsChatModelWithDefaultToolExecutionEligibilityPredicate() {
        chatContextRunner.run(context -> {
            OracleGenAiChatModel chatModel = context.getBean(OracleGenAiChatModel.class);

            assertThat(ReflectionTestUtils.getField(chatModel, "toolExecutionEligibilityPredicate"))
                    .isInstanceOf(DefaultToolExecutionEligibilityPredicate.class);
        });
    }

    @Test
    void configuresChatModelExtensionBeans() {
        chatContextRunner.withUserConfiguration(ChatExtensionBeans.class)
                .run(context -> {
                    OracleGenAiChatModel chatModel = context.getBean(OracleGenAiChatModel.class);

                    assertThat(ReflectionTestUtils.getField(chatModel, "toolCallingManager"))
                            .isSameAs(context.getBean(ToolCallingManager.class));
                    assertThat(ReflectionTestUtils.getField(chatModel, "toolExecutionEligibilityPredicate"))
                            .isSameAs(context.getBean(ToolExecutionEligibilityPredicate.class));
                    assertThat(ReflectionTestUtils.getField(chatModel, "retryTemplate"))
                            .isSameAs(context.getBean(RetryTemplate.class));
                });
    }

    @Test
    void createsEmbeddingModelWhenOracleIsSelected() {
        embeddingContextRunner.run(context -> {
            assertThat(context).hasSingleBean(EmbeddingModel.class);
            assertThat(context).hasSingleBean(OracleGenAiEmbeddingModel.class);
        });
    }

    @Test
    void createsEmbeddingModelWhenSelectorIsMissing() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GenAiAutoConfiguration.class))
                .withUserConfiguration(TestOciBeans.class)
                .withPropertyValues(
                        CHAT_DISABLED,
                        PropertyNames.EMBEDDING_CONFIG_PREFIX + ".compartment-id=compartment",
                        PropertyNames.EMBEDDING_CONFIG_PREFIX + ".model=model")
                .run(context -> {
                    assertThat(context).hasSingleBean(EmbeddingModel.class);
                    assertThat(context).hasSingleBean(OracleGenAiEmbeddingModel.class);
                });
    }

    @Test
    void doesNotCreateEmbeddingModelWhenOracleIsNotSelected() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GenAiAutoConfiguration.class))
                .withUserConfiguration(TestOciBeans.class)
                .withPropertyValues(
                        CHAT_DISABLED,
                        EMBEDDING_DISABLED)
                .run(context -> assertThat(context).doesNotHaveBean(EmbeddingModel.class));
    }

    @Test
    void backsOffWhenUserProvidesEmbeddingModel() {
        embeddingContextRunner.withUserConfiguration(UserEmbeddingModelConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(EmbeddingModel.class);
                    assertThat(context).doesNotHaveBean(OracleGenAiEmbeddingModel.class);
                });
    }

    @Test
    void bindsEmbeddingOptions() {
        embeddingContextRunner
                .withPropertyValues(
                        PropertyNames.EMBEDDING_CONFIG_PREFIX + ".dimensions=512",
                        PropertyNames.EMBEDDING_CONFIG_PREFIX + ".truncate=START",
                        PropertyNames.EMBEDDING_CONFIG_PREFIX + ".serving-mode=DEDICATED",
                        PropertyNames.EMBEDDING_CONFIG_PREFIX + ".endpoint-id=endpoint")
                .run(context -> {
                    EmbeddingProperties properties = context.getBean(EmbeddingProperties.class);
                    assertThat(properties.getDimensions()).isEqualTo(512);
                    assertThat(properties.getTruncate().name()).isEqualTo("START");
                    assertThat(properties.getServingMode().name()).isEqualTo("DEDICATED");
                    assertThat(properties.getEndpointId()).isEqualTo("endpoint");
                });
    }

    @Test
    void chatAndEmbeddingShareOciBeans() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GenAiAutoConfiguration.class))
                .withUserConfiguration(TestOciBeans.class)
                .withPropertyValues(
                        CHAT_SELECTED,
                        PropertyNames.CHAT_CONFIG_PREFIX + ".compartment-id=compartment",
                        PropertyNames.CHAT_CONFIG_PREFIX + ".model=chat-model",
                        EMBEDDING_SELECTED,
                        PropertyNames.EMBEDDING_CONFIG_PREFIX + ".compartment-id=compartment",
                        PropertyNames.EMBEDDING_CONFIG_PREFIX + ".model=embedding-model")
                .run(context -> {
                    assertThat(context).hasSingleBean(ChatModel.class);
                    assertThat(context).hasSingleBean(EmbeddingModel.class);
                    assertThat(context).hasSingleBean(BasicAuthenticationDetailsProvider.class);
                    assertThat(context).hasSingleBean(GenerativeAiInference.class);
                });
    }

    @Test
    void backsOffWhenUserProvidesAuthProvider() {
        chatContextRunner.run(context ->
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

    @Configuration(proxyBeanMethods = false)
    static class UserEmbeddingModelConfiguration {

        @Bean
        EmbeddingModel embeddingModel() {
            return new EmbeddingModel() {
                @Override
                public EmbeddingResponse call(EmbeddingRequest request) {
                    return new EmbeddingResponse(List.of(new Embedding(new float[] { 0.1f }, 0)));
                }

                @Override
                public float[] embed(Document document) {
                    return new float[] { 0.1f };
                }
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ChatExtensionBeans {

        @Bean
        ToolCallingManager toolCallingManager() {
            return ToolCallingManager.builder().build();
        }

        @Bean
        ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate() {
            return (chatOptions, chatResponse) -> false;
        }

        @Bean
        RetryTemplate retryTemplate() {
            return new RetryTemplate();
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

}
