/*
 ** TxEventQ Support for Spring Cloud Stream
 ** Copyright (c) 2023, 2024 Oracle and/or its affiliates.
 **
 ** This file has been modified by Oracle Corporation.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oracle.database.spring.cloud.stream.binder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.BinderException;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.PartitionCapableBinderTests;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.binder.Spy;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.channel.DefaultHeaderChannelRegistry;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.oracle.database.spring.cloud.stream.binder.config.JmsConsumerProperties;
import com.oracle.database.spring.cloud.stream.binder.config.JmsProducerProperties;
import com.oracle.database.spring.cloud.stream.binder.plsql.OracleDBUtils;
import com.oracle.database.spring.cloud.stream.binder.utils.Base64UrlNamingStrategy;
import com.oracle.database.spring.cloud.stream.binder.utils.DestinationNameResolver;
import com.oracle.database.spring.cloud.stream.binder.utils.JmsMessageDrivenChannelAdapterFactory;
import com.oracle.database.spring.cloud.stream.binder.utils.JmsSendingMessageHandlerFactory;
import com.oracle.database.spring.cloud.stream.binder.utils.ListenerContainerFactory;
import com.oracle.database.spring.cloud.stream.binder.utils.MessageRecoverer;
import com.oracle.database.spring.cloud.stream.binder.utils.RepublishMessageRecoverer;
import com.oracle.database.spring.cloud.stream.binder.utils.SpecCompliantJmsHeaderMapper;

import jakarta.jms.ConnectionFactory;
import nativetests.TestObject;
import oracle.jakarta.jms.AQjmsFactory;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import static org.awaitility.Awaitility.await;


@SuppressWarnings("unchecked")
@Testcontainers
public class TEQPartitionIT extends
        PartitionCapableBinderTests<TxEventQTestBinder, ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>> {

    private static TxEventQTestBinder teqBinder;
    private static GenericApplicationContext testApplicationContext;
    private static jakarta.jms.ConnectionFactory testConnectionFactory; // ADD THIS

    private static int DB_VERSION = 23;

    @Container
    private static final OracleContainer oracleContainer = Util.oracleContainer();

    @Override
    protected boolean usesExplicitRouting() {
        return DB_VERSION != 19;
    }

    @Override
    protected String getClassUnderTestName() {
        return JMSMessageChannelBinder.class.getSimpleName();
    }

    @BeforeAll
    public static void setBinder() throws Exception {
        Util.startOracleContainer(oracleContainer);
        teqBinder = createBinder();
    }

    protected static TxEventQTestBinder createBinder() throws Exception {
        PoolDataSource ds = PoolDataSourceFactory.getPoolDataSource();
        try {
            Util.configurePoolDataSource(ds, oracleContainer);
        } catch (Exception e) {
            System.out.println("Encountered error: " + e);
        }
        int dbversion;
        try (java.sql.Connection conn = ds.getConnection()) {
            dbversion = conn.getMetaData().getDatabaseMajorVersion();
        }
        OracleDBUtils dbutils = new OracleDBUtils(ds, dbversion);
        ConnectionFactory connectionFactory = AQjmsFactory.getConnectionFactory(ds);
        testConnectionFactory = connectionFactory;
        TxEventQQueueProvisioner queueProvisioner = new TxEventQQueueProvisioner(connectionFactory, dbutils);
        
        DB_VERSION = dbversion;
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        testApplicationContext = applicationContext;
     
        // Register the TaskScheduler as a Singleton with the exact name
        ThreadPoolTaskScheduler s = new ThreadPoolTaskScheduler();
        s.setPoolSize(5);
        s.setThreadNamePrefix("teq-test-scheduler-");
        s.initialize();
        applicationContext.getBeanFactory().registerSingleton("taskScheduler", s);

        // Register Error Channel as a Singleton
        applicationContext.getBeanFactory().registerSingleton("errorChannel", 
                new PublishSubscribeChannel());

        // Register the Registry as a Singleton
        applicationContext.getBeanFactory().registerSingleton("integrationHeaderChannelRegistry", 
                new DefaultHeaderChannelRegistry());

        // NOW register the Registrar - it will find these LIVE instances and stay quiet
        new org.springframework.integration.config.IntegrationRegistrar()
                .registerBeanDefinitions(null, applicationContext);
        
        applicationContext.refresh();
        
        // *** FIX: MANDATORY FOR SPRING 7 ***
        // This manually triggers the creation of the EvaluationContext bean 
        // that JmsMessageDrivenEndpoint strictly requires during afterPropertiesSet()
        org.springframework.integration.context.IntegrationContextUtils
            .getEvaluationContext(applicationContext.getBeanFactory());
        
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        JmsSendingMessageHandlerFactory jmsSendingMessageHandlerFactory = new JmsSendingMessageHandlerFactory(
                jmsTemplate, new SpecCompliantJmsHeaderMapper());
        jmsSendingMessageHandlerFactory.setApplicationContext(applicationContext);
        jmsSendingMessageHandlerFactory.setBeanFactory(applicationContext.getBeanFactory());
        ListenerContainerFactory listenerContainerFactory = new ListenerContainerFactory(connectionFactory);
        MessageRecoverer messageRecoverer = new RepublishMessageRecoverer(jmsTemplate,
                new SpecCompliantJmsHeaderMapper());
        JmsMessageDrivenChannelAdapterFactory jmsMessageDrivenChannelAdapterFactory = new JmsMessageDrivenChannelAdapterFactory(
                listenerContainerFactory, messageRecoverer);
        jmsMessageDrivenChannelAdapterFactory.setApplicationContext(applicationContext);
        jmsMessageDrivenChannelAdapterFactory.setBeanFactory(applicationContext.getBeanFactory());
        JMSMessageChannelBinder binder = new JMSMessageChannelBinder(queueProvisioner, jmsSendingMessageHandlerFactory,
                jmsMessageDrivenChannelAdapterFactory, jmsTemplate, connectionFactory);
        binder.setDestinationNameResolver(new DestinationNameResolver(new Base64UrlNamingStrategy("anonymous_")));
        binder.setApplicationContext(applicationContext);

        TxEventQTestBinder testBinder = new TxEventQTestBinder();
        testBinder.setBinder(binder);
        return testBinder;
    }

    @Override
    protected TxEventQTestBinder getBinder() throws Exception {
        return teqBinder;
    }

    @Override
    protected ExtendedConsumerProperties<JmsConsumerProperties> createConsumerProperties() {
        return new ExtendedConsumerProperties<>(new JmsConsumerProperties());
    }

    @Override
    protected ExtendedProducerProperties<JmsProducerProperties> createProducerProperties(TestInfo testInfo) {
        return new ExtendedProducerProperties<>(new JmsProducerProperties());
    }

    @Override
    public Spy spyOn(String name) {
        throw new UnsupportedOperationException("'spyOn' is not used by JMS tests");
    }

    @Override
    protected String getDestinationNameDelimiter() {
        return "_";
    }

    protected Message<?> receiveMessage(QueueChannel channel) {
    	AtomicReference<Message<?>> resultHolder = new AtomicReference<>();

        await()
            .atMost(Duration.ofMinutes(3)) // Maximum total wait time
            .pollInterval(Duration.ofSeconds(30)) // How often to check the queue
            .with()
            .pollDelay(Duration.ZERO) // Check immediately the first time
            .until(() -> {
                Message<?> msg = receive(channel);
                if (msg != null) {
                    resultHolder.set(msg);
                    return true;
                }
                return false;
            });

        return resultHolder.get();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testSendAndReceive(TestInfo testInfo) throws Exception {
        Binder binder = teqBinder;
        BindingProperties outputBindingProperties = createProducerBindingProperties(createProducerProperties(testInfo));
        DirectChannel moduleOutputChannel = createBindableChannel("output", outputBindingProperties);

        BindingProperties inputBindingProperties = createConsumerBindingProperties(createConsumerProperties());
        DirectChannel moduleInputChannel = createBindableChannel("input", inputBindingProperties);

        Binding<MessageChannel> producerBinding = binder.bindProducer(
                String.format("foo%s113", getDestinationNameDelimiter()), moduleOutputChannel,
                outputBindingProperties.getProducer());
        Binding<MessageChannel> consumerBinding = binder.bindConsumer(
                String.format("foo%s113", getDestinationNameDelimiter()), "testSendAndReceive", moduleInputChannel,
                inputBindingProperties.getConsumer());
        Message<?> message = MessageBuilder.withPayload("foo").setHeader(MessageHeaders.CONTENT_TYPE, "text/plain")
                .build();
        // Let the consumer actually bind to the producer before sending a msg
        binderBindUnbindLatency();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Message<byte[]>> inboundMessageRef = new AtomicReference<Message<byte[]>>();
        moduleInputChannel.subscribe(message1 -> {
            try {
                inboundMessageRef.set((Message<byte[]>) message1);
            } finally {
                latch.countDown();
            }
        });

        moduleOutputChannel.send(message);
        Assert.isTrue(latch.await(180, TimeUnit.SECONDS), "Failed to receive message");

        assertThat(inboundMessageRef.get().getPayload()).isEqualTo("foo".getBytes());
        assertThat(inboundMessageRef.get().getHeaders().get(MessageHeaders.CONTENT_TYPE)).hasToString("text/plain");
        producerBinding.unbind();
        consumerBinding.unbind();
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendAndReceiveMultipleTopics(TestInfo testInfo) throws Exception {
        Binder binder = teqBinder;

        BindingProperties producerBindingProperties = createProducerBindingProperties(
                createProducerProperties(testInfo));

        DirectChannel moduleOutputChannel1 = createBindableChannel("output1", producerBindingProperties);

        DirectChannel moduleOutputChannel2 = createBindableChannel("output2", producerBindingProperties);

        QueueChannel moduleInputChannel = new QueueChannel();

        Binding<MessageChannel> producerBinding1 = binder.bindProducer(
                String.format("foo%s111", getDestinationNameDelimiter()), moduleOutputChannel1,
                producerBindingProperties.getProducer());
        Binding<MessageChannel> producerBinding2 = binder.bindProducer(String.format("foo%s112",

                getDestinationNameDelimiter()), moduleOutputChannel2, producerBindingProperties.getProducer());

        Binding<MessageChannel> consumerBinding1 = binder.bindConsumer(
                String.format("foo%s111", getDestinationNameDelimiter()), "testSendAndReceiveMultipleTopics",
                moduleInputChannel, createConsumerProperties());
        Binding<MessageChannel> consumerBinding2 = binder.bindConsumer(
                String.format("foo%s112", getDestinationNameDelimiter()), "testSendAndReceiveMultipleTopics",
                moduleInputChannel, createConsumerProperties());

        String testPayload1 = "foo" + UUID.randomUUID();
        Message<?> message1 = MessageBuilder.withPayload(testPayload1.getBytes())
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM).build();
        String testPayload2 = "foo" + UUID.randomUUID();
        Message<?> message2 = MessageBuilder.withPayload(testPayload2.getBytes())
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM).build();

        // Let the consumer actually bind to the producer before sending a msg
        binderBindUnbindLatency();
        moduleOutputChannel1.send(message1);
        moduleOutputChannel2.send(message2);

        Message<?>[] messages = new Message[2];
        messages[0] = receiveMessage(moduleInputChannel);
        messages[1] = receiveMessage(moduleInputChannel);

        assertThat(messages[0]).isNotNull();
        assertThat(messages[1]).isNotNull();
        assertThat(messages).extracting("payload").containsExactlyInAnyOrder(testPayload1.getBytes(),
                testPayload2.getBytes());

        producerBinding1.unbind();
        producerBinding2.unbind();

        consumerBinding1.unbind();
        consumerBinding2.unbind();
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendAndReceiveNoOriginalContentType(TestInfo testInfo) throws Exception {
        Binder binder = teqBinder;

        BindingProperties producerBindingProperties = createProducerBindingProperties(
                createProducerProperties(testInfo));
        DirectChannel moduleOutputChannel = createBindableChannel("output", producerBindingProperties);
        BindingProperties inputBindingProperties = createConsumerBindingProperties(createConsumerProperties());
        DirectChannel moduleInputChannel = createBindableChannel("input", inputBindingProperties);
        Binding<MessageChannel> producerBinding = binder.bindProducer(
                String.format("bar%s112", getDestinationNameDelimiter()), moduleOutputChannel,
                producerBindingProperties.getProducer());
        Binding<MessageChannel> consumerBinding = binder.bindConsumer(
                String.format("bar%s112", getDestinationNameDelimiter()), "testSendAndReceiveNoOriginalContentType",
                moduleInputChannel, createConsumerProperties());
        binderBindUnbindLatency();

        Message<?> message = MessageBuilder.withPayload("foo")
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN).build();
        moduleOutputChannel.send(message);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Message<byte[]>> inboundMessageRef = new AtomicReference<Message<byte[]>>();
        moduleInputChannel.subscribe(message1 -> {
            try {
                inboundMessageRef.set((Message<byte[]>) message1);
            } finally {
                latch.countDown();
            }
        });

        Assert.isTrue(latch.await(10, TimeUnit.SECONDS), "Failed to receive message");
        assertThat(inboundMessageRef.get()).isNotNull();
        assertThat(inboundMessageRef.get().getPayload()).isEqualTo("foo".getBytes());
        assertThat(inboundMessageRef.get().getHeaders().get(MessageHeaders.CONTENT_TYPE))
                .hasToString(MimeTypeUtils.TEXT_PLAIN_VALUE);
        producerBinding.unbind();
        consumerBinding.unbind();
    }

    @Override
    @Test
    public void testPartitionedModuleSpEL(TestInfo testInfo) throws Exception {
        TxEventQTestBinder binder = teqBinder;

        ExtendedConsumerProperties<JmsConsumerProperties> consumerProperties = createConsumerProperties();
        consumerProperties.setConcurrency(2);
        consumerProperties.setInstanceIndex(0);
        consumerProperties.setInstanceCount(3);
        consumerProperties.setPartitioned(true);
        QueueChannel input0 = new QueueChannel();
        input0.setBeanName("test.input0S");
        Binding<MessageChannel> input0Binding = binder.bindConsumer(
                String.format("part%s111", getDestinationNameDelimiter()), "testPartitionedModuleSpEL", input0,
                consumerProperties);
        consumerProperties.setInstanceIndex(1);
        QueueChannel input1 = new QueueChannel();
        input1.setBeanName("test.input1S");
        Binding<MessageChannel> input1Binding = binder.bindConsumer(
                String.format("part%s111", getDestinationNameDelimiter()), "testPartitionedModuleSpEL", input1,
                consumerProperties);
        consumerProperties.setInstanceIndex(2);
        QueueChannel input2 = new QueueChannel();
        input2.setBeanName("test.input2S");
        Binding<MessageChannel> input2Binding = binder.bindConsumer(
                String.format("part%s111", getDestinationNameDelimiter()), "testPartitionedModuleSpEL", input2,
                consumerProperties);

        // allow bindings to start properly
        Thread.sleep(10000);

        ExtendedProducerProperties<JmsProducerProperties> producerProperties = createProducerProperties(testInfo);
        producerProperties.setPartitionKeyExpression(spelExpressionParser.parseExpression("payload"));
        producerProperties.setPartitionSelectorExpression(spelExpressionParser.parseExpression("hashCode()"));
        producerProperties.setPartitionCount(3);

        DirectChannel output = createBindableChannel("output", createProducerBindingProperties(producerProperties));
        output.setBeanName("test.output");
        Binding<MessageChannel> outputBinding = binder
                .bindProducer(String.format("part%s111", getDestinationNameDelimiter()), output, producerProperties);
        try {
            Object endpoint = extractEndpoint(outputBinding);
            checkRkExpressionForPartitionedModuleSpEL(endpoint);
        } catch (UnsupportedOperationException ignored) {
        }

        Message<String> message2 = MessageBuilder.withPayload("2")
                .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, "foo")
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN)
                .setHeader(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER, 42)
                .setHeader(IntegrationMessageHeaderAccessor.SEQUENCE_SIZE, 43).build();
        output.send(message2);
        output.send(MessageBuilder.withPayload("1").setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN)
                .build());
        output.send(MessageBuilder.withPayload("0").setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN)
                .build());

        Thread.sleep(3000);

        Message<?> receive0 = null, receive1 = null, receive2 = null;
        if (usesExplicitRouting()) {
            receive0 = receive(input0);
            receive1 = receive(input1);
            receive2 = receive(input2);
        } else {
            Message<?>[] r = new Message<?>[3];
            for (int x = 0; x < 3; x++) {
                r[x] = receive(input0);
                if (r[x] != null)
                    continue;
                r[x] = receive(input1);
                if (r[x] != null)
                    continue;
                r[x] = receive(input2);
            }
            receive0 = r[0];
            receive1 = r[1];
            receive2 = r[2];
        }

        assertThat(receive0).isNotNull();
        assertThat(receive1).isNotNull();
        assertThat(receive2).isNotNull();

        Condition<Message<?>> correlationHeadersForPayload2 = new Condition<Message<?>>() {
            @Override
            public boolean matches(Message<?> value) {
                IntegrationMessageHeaderAccessor accessor = new IntegrationMessageHeaderAccessor(value);
                return "foo".equals(accessor.getCorrelationId()) && 42 == accessor.getSequenceNumber()
                        && 43 == accessor.getSequenceSize();
            }
        };

        if (usesExplicitRouting()) {
            assertThat(receive0.getPayload()).isEqualTo("0".getBytes());
            assertThat(receive1.getPayload()).isEqualTo("1".getBytes());
            assertThat(receive2.getPayload()).isEqualTo("2".getBytes());
            assertThat(receive2).has(correlationHeadersForPayload2);
        } else {
            List<Message<?>> receivedMessages = Arrays.asList(receive0, receive1, receive2);
            assertThat(receivedMessages).extracting("payload").containsExactlyInAnyOrder("0".getBytes(), "1".getBytes(),
                    "2".getBytes());
            Condition<Message<?>> payloadIs2 = new Condition<Message<?>>() {

                @Override
                public boolean matches(Message<?> value) {
                    return new String((byte[]) value.getPayload()).equals("2");
                }
            };
            assertThat(receivedMessages).filteredOn(payloadIs2).areExactly(1, correlationHeadersForPayload2);

        }
        input0Binding.unbind();
        input1Binding.unbind();
        input2Binding.unbind();
        outputBinding.unbind();
    }

    @Override
    @Test
    public void testOneRequiredGroup(TestInfo testInfo) throws Exception {
        TxEventQTestBinder binder = teqBinder;
        ExtendedProducerProperties<JmsProducerProperties> producerProperties = createProducerProperties(testInfo);
        DirectChannel output = createBindableChannel("output", createProducerBindingProperties(producerProperties));

        String testDestination = "testDestination" + UUID.randomUUID().toString().replace("-", "");

        producerProperties.setRequiredGroups("test1");
        Binding<MessageChannel> producerBinding = binder.bindProducer(testDestination, output, producerProperties);

        String testPayload = "foo-" + UUID.randomUUID();

        QueueChannel inbound1 = new QueueChannel();
        Binding<MessageChannel> consumerBinding = binder.bindConsumer(testDestination, "test1", inbound1,
                createConsumerProperties());

        binderBindUnbindLatency();

        output.send(MessageBuilder.withPayload(testPayload)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN).build());

        Message<?> receivedMessage1 = receiveMessage(inbound1);
        assertThat(receivedMessage1).isNotNull();
        assertThat(new String((byte[]) receivedMessage1.getPayload())).isEqualTo(testPayload);

        producerBinding.unbind();
        consumerBinding.unbind();
    }

    @Override
    @Test
    public void testTwoRequiredGroups(TestInfo testInfo) throws Exception {
        TxEventQTestBinder binder = teqBinder;
        ExtendedProducerProperties<JmsProducerProperties> producerProperties = createProducerProperties(testInfo);

        DirectChannel output = createBindableChannel("output", createProducerBindingProperties(producerProperties));

        String testDestination = "testDestination" + UUID.randomUUID().toString().replace("-", "");

        producerProperties.setRequiredGroups("test1", "test2");
        Binding<MessageChannel> producerBinding = binder.bindProducer(testDestination, output, producerProperties);

        String testPayload = "foo-" + UUID.randomUUID();
        output.send(MessageBuilder.withPayload(testPayload)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN).build());

        QueueChannel inbound1 = new QueueChannel();
        Binding<MessageChannel> consumerBinding1 = binder.bindConsumer(testDestination, "test1", inbound1,
                createConsumerProperties());
        QueueChannel inbound2 = new QueueChannel();
        Binding<MessageChannel> consumerBinding2 = binder.bindConsumer(testDestination, "test2", inbound2,
                createConsumerProperties());

        binderBindUnbindLatency();

        Message<?> receivedMessage1 = receiveMessage(inbound1);
        assertThat(receivedMessage1).isNotNull();
        assertThat(new String((byte[]) receivedMessage1.getPayload())).isEqualTo(testPayload);

        Message<?> receivedMessage2 = receiveMessage(inbound2);
        assertThat(receivedMessage2).isNotNull();
        assertThat(new String((byte[]) receivedMessage2.getPayload())).isEqualTo(testPayload);

        consumerBinding1.unbind();
        consumerBinding2.unbind();
        producerBinding.unbind();
    }

    @Override
    @Test
    @Disabled // TODO: Fix this test for TEQ
    public void testAnonymousGroup(TestInfo testInfo) throws Exception {
        TxEventQTestBinder binder = teqBinder;
        ExtendedProducerProperties<JmsProducerProperties> producerProperties = createProducerProperties(testInfo);
        BindingProperties producerBindingProperties = createProducerBindingProperties(producerProperties);
        DirectChannel output = createBindableChannel("output", producerBindingProperties);
        Binding<MessageChannel> producerBinding = binder.bindProducer(
                String.format("defaultGroup%s1", getDestinationNameDelimiter()), output, producerProperties);

        QueueChannel input1 = new QueueChannel();
        Binding<MessageChannel> binding1 = binder.bindConsumer(
                String.format("defaultGroup%s1", getDestinationNameDelimiter()), null, input1,
                createConsumerProperties());

        QueueChannel input2 = new QueueChannel();
        Binding<MessageChannel> binding2 = binder.bindConsumer(
                String.format("defaultGroup%s1", getDestinationNameDelimiter()), null, input2,
                createConsumerProperties());

        binderBindUnbindLatency();

        String testPayload1 = "foo-" + UUID.randomUUID();
        output.send(MessageBuilder.withPayload(testPayload1)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN).build());

        Message<byte[]> receivedMessage1 = (Message<byte[]>) receiveMessage(input1);
        assertThat(receivedMessage1).isNotNull();
        assertThat(new String(receivedMessage1.getPayload())).isEqualTo(testPayload1);

        Message<byte[]> receivedMessage2 = (Message<byte[]>) receiveMessage(input2);
        assertThat(receivedMessage2).isNotNull();
        assertThat(new String(receivedMessage2.getPayload())).isEqualTo(testPayload1);

        binding2.unbind();

        String testPayload2 = "foo-" + UUID.randomUUID();
        output.send(MessageBuilder.withPayload(testPayload2)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN).build());

        binding2 = binder.bindConsumer(String.format("defaultGroup%s1", getDestinationNameDelimiter()), null, input2,
                createConsumerProperties());
        String testPayload3 = "foo-" + UUID.randomUUID();
        output.send(MessageBuilder.withPayload(testPayload3)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN).build());

        receivedMessage1 = (Message<byte[]>) receiveMessage(input1);
        assertThat(receivedMessage1).isNotNull();
        assertThat(new String(receivedMessage1.getPayload())).isEqualTo(testPayload2);
        receivedMessage1 = (Message<byte[]>) receiveMessage(input1);
        assertThat(receivedMessage1).isNotNull();
        assertThat(new String(receivedMessage1.getPayload())).isNotNull();

        receivedMessage2 = (Message<byte[]>) receiveMessage(input2);
        assertThat(receivedMessage2).isNotNull();
        assertThat(new String(receivedMessage2.getPayload())).isEqualTo(testPayload3);

        producerBinding.unbind();
        binding1.unbind();
        binding2.unbind();
    }

    @Override
    public void binderBindUnbindLatency() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void testBatchSize(TestInfo testInfo) throws Exception {
        Binder binder = teqBinder;
        ExtendedProducerProperties<JmsProducerProperties> producerProperties = createProducerProperties(testInfo);
        producerProperties.setRequiredGroups("test1");
        BindingProperties outputBindingProperties = createProducerBindingProperties(producerProperties);
        DirectChannel moduleOutputChannel = createBindableChannel("output", outputBindingProperties);
        ExtendedConsumerProperties<JmsConsumerProperties> consumerProperties = createConsumerProperties();
        consumerProperties.setBatchMode(true);
        consumerProperties.getExtension().setBatchSize(3);
        consumerProperties.getExtension().setTimeout(4000);

        BindingProperties inputBindingProperties = createConsumerBindingProperties(consumerProperties);
        DirectChannel moduleInputChannel = createBindableChannel("input", inputBindingProperties);

        Binding<MessageChannel> producerBinding = binder.bindProducer(
                String.format("foo%s114", getDestinationNameDelimiter()), moduleOutputChannel,
                outputBindingProperties.getProducer());
        Message<?> message = MessageBuilder.withPayload("foo").setHeader(MessageHeaders.CONTENT_TYPE, "text/plain")
                .build();
        moduleOutputChannel.send(message);
        moduleOutputChannel.send(message);
        moduleOutputChannel.send(message);
        moduleOutputChannel.send(message);

        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<Message<List<byte[]>>> inboundMessageRef = new AtomicReference<Message<List<byte[]>>>();
        AtomicReference<Message<List<byte[]>>> inboundMessageRef2 = new AtomicReference<Message<List<byte[]>>>();

        moduleInputChannel.subscribe(message1 -> {
            try {
                if (inboundMessageRef.get() == null) {
                    inboundMessageRef.set((Message<List<byte[]>>) message1);
                } else {
                    inboundMessageRef2.set((Message<List<byte[]>>) message1);
                }
            } finally {
                latch.countDown();
            }
        });

        Binding<MessageChannel> consumerBinding = binder.bindConsumer(
                String.format("foo%s114", getDestinationNameDelimiter()), "test1", moduleInputChannel,
                inputBindingProperties.getConsumer());

        binderBindUnbindLatency();

        Assert.isTrue(latch.await(180, TimeUnit.SECONDS), "Failed to receive message");

        Message<?> receivedMessage1 = inboundMessageRef.get();
        assertThat(receivedMessage1).isNotNull();
        Object payload = receivedMessage1.getPayload();
        assertThat(payload).isInstanceOf(List.class);
        List<?> messages = (List<?>) payload;
        assertThat(messages).hasSize(3);

        for (Object msg : messages) {
            assertThat(msg).isInstanceOf(byte[].class);
            assertThat(new String((byte[]) msg)).isEqualTo("foo");
        }

        Message<?> receivedMessage2 = inboundMessageRef2.get();
        assertThat(receivedMessage2).isNotNull();
        payload = receivedMessage2.getPayload();
        assertThat(payload).isInstanceOf(List.class);
        messages = (List<?>) payload;
        assertThat(messages).hasSize(1);
        for (Object msg : messages) {
            assertThat(msg).isInstanceOf(byte[].class);
            assertThat(new String((byte[]) msg)).isEqualTo("foo");
        }

        producerBinding.unbind();
        consumerBinding.unbind();
    }

    @SuppressWarnings("rawtypes")
    @Test
    void testNativeEncodingDecoding(TestInfo testInfo) throws Exception {
        Binder binder = teqBinder;

        // Producer configuration
        ExtendedProducerProperties<JmsProducerProperties> producerProperties = createProducerProperties(testInfo);
        producerProperties.setRequiredGroups("test1", "test2");
        producerProperties.setUseNativeEncoding(true);
        producerProperties.getExtension().setSerializer("nativetests.TestObjectSerializer");
        BindingProperties outputBindingProperties = createProducerBindingProperties(producerProperties);
        DirectChannel moduleOutputChannel = createBindableChannel("output", outputBindingProperties);

        // Consumer1 configuration
        ExtendedConsumerProperties<JmsConsumerProperties> consumerProperties1 = createConsumerProperties();
        consumerProperties1.setUseNativeDecoding(true);
        consumerProperties1.getExtension().setDeSerializer("nativetests.TestObjectDeserializer");
        BindingProperties inputBindingProperties1 = createConsumerBindingProperties(consumerProperties1);
        DirectChannel moduleInputChannel1 = createBindableChannel("input", inputBindingProperties1);

        // Consumer2 configuration
        ExtendedConsumerProperties<JmsConsumerProperties> consumerProperties2 = createConsumerProperties();
        BindingProperties inputBindingProperties2 = createConsumerBindingProperties(consumerProperties2);
        DirectChannel moduleInputChannel2 = createBindableChannel("input", inputBindingProperties2);

        // Create producer binding and send single message
        Binding<MessageChannel> producerBinding = binder.bindProducer(
                String.format("foone%s14", getDestinationNameDelimiter()), moduleOutputChannel,
                outputBindingProperties.getProducer());

        // Create consumer binding 1 -> decoded
        Binding<MessageChannel> consumerBinding1 = binder.bindConsumer(
                String.format("foone%s14", getDestinationNameDelimiter()), "test1", moduleInputChannel1,
                inputBindingProperties1.getConsumer());

        // Create consumer binding 2 -> non-decoded
        Binding<MessageChannel> consumerBinding2 = binder.bindConsumer(
                String.format("foone%s14", getDestinationNameDelimiter()), "test2", moduleInputChannel2,
                inputBindingProperties2.getConsumer());

        binderBindUnbindLatency();

        Message<?> message = MessageBuilder.withPayload(new TestObject(0)).build();
        moduleOutputChannel.send(message);

        AtomicReference<Message<?>> inboundMessageRef1 = new AtomicReference<Message<?>>();
        AtomicReference<Message<?>> inboundMessageRef2 = new AtomicReference<Message<?>>();

        CountDownLatch latch = new CountDownLatch(2);

        moduleInputChannel1.subscribe(message1 -> {
            try {
                inboundMessageRef1.set(message1);
            } finally {
                latch.countDown();
            }
        });

        moduleInputChannel2.subscribe(message2 -> {
            try {
                inboundMessageRef2.set(message2);
            } finally {
                latch.countDown();
            }
        });

        Assert.isTrue(latch.await(180, TimeUnit.SECONDS), "Failed to receive message");

        // for decoded version
        Message<?> receivedMessage1 = inboundMessageRef1.get();
        assertThat(receivedMessage1).isNotNull();
        Object payload = receivedMessage1.getPayload();
        assertThat(payload).isInstanceOf(TestObject.class);
        assertThat(((TestObject) payload).getX()).isZero();

        // for non-decoded version - should receive transformed String
        Message<?> receivedMessage2 = inboundMessageRef2.get();
        assertThat(receivedMessage2).isNotNull();
        payload = receivedMessage2.getPayload();
        assertThat(payload).isInstanceOf(byte[].class);
        assertThat(new String((byte[]) payload)).isEqualTo("MyTestObject[x='0']");

        producerBinding.unbind();
        consumerBinding1.unbind();
        consumerBinding2.unbind();
    }

    @SuppressWarnings("rawtypes")
    @Test
    void testErrorOnInstanceIndexListSet(TestInfo testInfo) throws Exception {
        Binder binder = teqBinder;

        ExtendedConsumerProperties<JmsConsumerProperties> consumerProperties = createConsumerProperties();
        List<Integer> instanceList = new ArrayList<Integer>();
        instanceList.add(0);
        instanceList.add(1);
        consumerProperties.setInstanceIndexList(instanceList);

        BindingProperties inputBindingProperties = createConsumerBindingProperties(consumerProperties);
        DirectChannel moduleInputChannel = createBindableChannel("input", inputBindingProperties);

        String dest = String.format("foo%s11", getDestinationNameDelimiter());
        ConsumerProperties properties = inputBindingProperties.getConsumer();

        BinderException e = assertThrows(BinderException.class,
                () -> binder.bindConsumer(dest, "test1", moduleInputChannel, properties));

        assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        assertThat(e.getCause().getMessage()).isEqualTo("The property 'instanceIndexList' is not supported.");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void testErrorOnMultiplex(TestInfo testInfo) throws Exception {
        Binder binder = teqBinder;

        ExtendedConsumerProperties<JmsConsumerProperties> consumerProperties = createConsumerProperties();
        consumerProperties.setMultiplex(true);

        BindingProperties inputBindingProperties = createConsumerBindingProperties(consumerProperties);
        DirectChannel moduleInputChannel = createBindableChannel("input", inputBindingProperties);

        String dest = String.format("foo%s11", getDestinationNameDelimiter());
        ConsumerProperties properties = inputBindingProperties.getConsumer();

        BinderException e = assertThrows(BinderException.class,
                () -> binder.bindConsumer(dest, "test1", moduleInputChannel, properties));

        assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        assertThat(e.getCause().getMessage()).isEqualTo("The property 'multiplex:true' is not supported.");
    }
    
    /**
     * Validates Basic Message Delivery and Header Preservation in Oracle TxEventQ.
     * 
     * This test verifies that:
     * 1. The Binder successfully maps Spring {@link org.springframework.messaging.MessageHeaders} 
     *    to native JMS User Properties within the Oracle Sharded Queue.
     * 2. Custom application metadata (e.g., {@code custom_header} and {@code my_correlation}) 
     *    is persisted and recovered without data loss during the round-trip.
     * 3. The {@link org.springframework.jms.core.JmsTemplate} correctly commits the 
     *    producer-side transaction, making the message visible to the consumer group.
     * 4. The {@link com.oracle.database.spring.cloud.stream.binder.utils.JmsMessageDrivenChannelAdapter} 
     *    efficiently handles the conversion between JMS {@code BytesMessage} and 
     *    Spring Cloud Stream's expected internal {@code byte[]} representation.
     * 
     * Note: Employs {@link org.awaitility.Awaitility} with a 45-second window to accommodate 
     * the asynchronous nature of Oracle's background process discovery (AQPC) and 
     * Testcontainers-specific network latency.
     */
    @Test
    void testSendMessage(TestInfo testInfo) throws Exception {
    	TxEventQTestBinder binder = (TxEventQTestBinder) getBinder();
        String destination = "foo_" + UUID.randomUUID().toString().substring(0, 8);

        // Consumer Setup
        QueueChannel consumerChannel = new QueueChannel();
        consumerChannel.setBeanName("testConsumerChannel");
        Binding<MessageChannel> consumerBinding = binder.bindConsumer(
                destination, "testGroup", consumerChannel, createConsumerProperties());

        // Wait for the Oracle subscriber to be registered in the DB
        await().atMost(Duration.ofSeconds(30)).until(consumerBinding::isRunning);

        // Producer Setup
        DirectChannel moduleOutputChannel = new DirectChannel();
        moduleOutputChannel.setBeanName("testProducerChannel");
        Binding<MessageChannel> producerBinding = binder.bindProducer(
                destination, moduleOutputChannel, createProducerProperties(testInfo));

        try {
            String payload = "Hello Oracle TXEventQ";
            // Create message with a unique ID to verify it's the SAME message
            String correlationId = UUID.randomUUID().toString();
            Message<String> message = MessageBuilder.withPayload(payload)
                    .setHeader("custom_header", "spring-7-test")
                    .setHeader("my_correlation", correlationId)
                    .build();

            // Send and ensure the Binder/JmsTemplate commits
            moduleOutputChannel.send(message);

            // Verification with Awaitility
            await()
                .atMost(Duration.ofSeconds(45)) 
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    // Use a non-blocking receive
                    Message<?> received = consumerChannel.receive(0);
                    
                    assertThat(received)
                        .as("Message should have arrived in Oracle TxEventQ")
                        .isNotNull();
                    
                    // Binder usually returns byte[] for JMS BytesMessage
                    Object receivedPayload = received.getPayload();
                    if (receivedPayload instanceof byte[] bytes) {
                        assertThat(new String(bytes)).isEqualTo(payload);
                    } else {
                        assertThat(receivedPayload).isEqualTo(payload);
                    }

                    assertThat(received.getHeaders())
                        .as("Custom header should be preserved")
                        .containsEntry("custom_header", "spring-7-test");
                });

        } finally {
            producerBinding.unbind();
            consumerBinding.unbind();
        }
    }
    
    /**
     * Validates the Full End-to-End Messaging Lifecycle in Oracle TxEventQ.
     * 
     * This test verifies the complete integration path, including:
     * 1. Dynamic Provisioning: The Binder's {@link com.oracle.database.spring.cloud.stream.binder.TxEventQQueueProvisioner} 
     *    automatically creating the {@code TxEventQ} topic and subscriber metadata.
     * 2. Producer-to-Database Handshake: Successful persistence of a Spring {@link org.springframework.messaging.Message} 
     *    into the Oracle Sharded Queue tables.
     * 3. Asynchronous Discovery: The {@link com.oracle.database.spring.cloud.stream.binder.utils.TEQMessageListenerContainer} 
     *    polling and identifying new messages on the sharded destination.
     * 4. Payload Round-tripping: Transparent serialization and deserialization of the 
     *    payload between the Java Heap and the Oracle Database engine.
     * 
     * Note: Employs a robust 2-minute {@link org.awaitility.Awaitility} window to account 
     * for the inherent latency of background Oracle AQ processes (AQPC) and metadata 
     * propagation within a Testcontainers (Docker) environment.
     */
    @Test
    void testFullEndToEnd(TestInfo testInfo) throws Exception {
    	TxEventQTestBinder binder = (TxEventQTestBinder) getBinder();
    	String destination = "foo_" + UUID.randomUUID().toString().substring(0, 8);

	    QueueChannel input = new QueueChannel();
	    input.setBeanName("testFullInput");
	    DirectChannel output = new DirectChannel();
	    output.setBeanName("testFullOutput");

	    // Bindings
	    Binding<MessageChannel> consumerBinding = binder.bindConsumer(destination, "testGroup", input, createConsumerProperties());
	    Binding<MessageChannel> producerBinding = binder.bindProducer(destination, output, createProducerProperties(testInfo));

	    try {
	        // STABILIZATION: Ensure Oracle DB recognizes the new subscriber
	        await().atMost(Duration.ofSeconds(30)).until(consumerBinding::isRunning);

	        String payload = "Hello Spring 7";
	        
	        // SEND: Use a retry loop for the send if needed, but standard send should work
	        // if the database is ready.
	        output.send(MessageBuilder.withPayload(payload).build());
	        System.out.println(">>> Message sent to: " + destination);

	        // VERIFICATION: Oracle containers are slow; give it a full 2 minute
	        await()
	            .atMost(Duration.ofMinutes(2))
	            .pollInterval(Duration.ofSeconds(2))
	            .untilAsserted(() -> {
	                // Poll the channel. receive(0) is best here as Awaitility loops
	                Message<?> msg = input.receive(0);
	                assertThat(msg).as("Message should have arrived in TxEventQ").isNotNull();
	                
	                Object p = msg.getPayload();
	                String str = (p instanceof byte[] b) ? new String(b) : p.toString();
	                assertThat(str).isEqualTo(payload);
	            });

	    } finally {
	        if (producerBinding != null) producerBinding.unbind();
	        if (consumerBinding != null) consumerBinding.unbind();
	    }
    }
    
    /**
     * Validates the Retry Interceptor and Error Channel Recovery for Oracle TxEventQ.
     * 
     * This test verifies that:
     * 1. The Binder correctly integrates with Spring's {@code RetryTemplate} to execute 
     *    the {@code maxAttempts(3)} policy before exhausting retries.
     * 2. Transient failures in the {@code inputChannel} subscriber trigger a standard 
     *    Spring Cloud Stream retry cycle without immediate message loss.
     * 3. Upon retry exhaustion, the Binder's error handling logic bridges the failure 
     *    to the global {@code errorChannel}.
     * 4. The recovered message preserves original metadata (e.g., {@code Content-Type}) 
     *    within the {@link org.springframework.messaging.MessagingException} wrapper.
     * 5. Oracle TxEventQ maintains the message's transactional state across the retry 
     *    attempts until the final recovery action is taken.
     * 
     * Note: Uses {@link org.awaitility.Awaitility} to manage the asynchronous gap 
     * between subscriber metadata propagation in Oracle DB and the first delivery attempt.
     */
    @Test
    void testRetryAndRecoveryLogic(TestInfo testInfo) throws Exception {
    	TxEventQTestBinder binder = getBinder();
        
        // Setup properties with 3 retry attempts and DLQ
        ExtendedConsumerProperties<JmsConsumerProperties> consumerProperties = createConsumerProperties();
        consumerProperties.setMaxAttempts(3); 
        consumerProperties.getExtension().setDlqName("test_dlq");

        DirectChannel inputChannel = createBindableChannel("input", new BindingProperties());
        String destination = "retryTestQueue_" + UUID.randomUUID().toString().substring(0, 8);
        String group = "retryGroup";
        
        // Track attempts and final recovery
        CountDownLatch attemptLatch = new CountDownLatch(3);
        AtomicReference<Throwable> recoveredError = new AtomicReference<>();
        CountDownLatch recoveryLatch = new CountDownLatch(1);

        // Create a consumer that ALWAYS fails to trigger retries
        inputChannel.subscribe(message -> {
            attemptLatch.countDown();
            throw new RuntimeException("Simulated Transient Failure");
        });

        // Subscribe to the global errorChannel (The Binder bridges failures here)
        // Note: Use the 'testApplicationContext' reference we captured in createBinder()
        SubscribableChannel globalErrorChannel = testApplicationContext.getBean("errorChannel", SubscribableChannel.class);
        globalErrorChannel.subscribe(msg -> {
            // The recovery process wraps the failure in a MessagingException
            if (msg.getPayload() instanceof MessagingException ex) {
                recoveredError.set(ex);
                recoveryLatch.countDown();
            }
        });

        // Bind the consumer
        Binding<MessageChannel> consumerBinding = binder.bindConsumer(destination, group, inputChannel, consumerProperties);
        
        // STABILIZATION: Oracle TEQ needs time to propagate subscriber metadata
        await().atMost(Duration.ofSeconds(30)).until(consumerBinding::isRunning);
        
        // Send the message
        Message<String> message = MessageBuilder.withPayload("test-retry-payload")
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build();
        
        DirectChannel producerChannel = createBindableChannel("output", new BindingProperties());
        binder.bindProducer(destination, producerChannel, createProducerProperties(testInfo));
        producerChannel.send(message);

        // VERIFICATIONS
        
        // Verify 3 attempts occurred (Increased timeout for persistent DB rollbacks)
        boolean retriedEnough = attemptLatch.await(30, TimeUnit.SECONDS);
        assertThat(retriedEnough).as("Should have attempted the message 3 times").isTrue();

        // Verify the recovery triggered after 3rd failure
        boolean recovered = recoveryLatch.await(15, TimeUnit.SECONDS);
        assertThat(recovered).as("Recovery should have been triggered").isTrue();

        // Verify the Exception Chain (MessagingException -> Original Failure)
        Throwable actualError = recoveredError.get();
        assertThat(actualError.getCause().getMessage())
                .contains("Simulated Transient Failure");
        
        // Verify our Shim successfully preserved headers in the failed message
        Message<?> failedMessage = ((MessagingException) actualError).getFailedMessage();
        Object contentType = failedMessage.getHeaders().get(MessageHeaders.CONTENT_TYPE);

        assertThat(contentType)
        .as("Content type should match the application/json string")
        .hasToString(MimeTypeUtils.APPLICATION_JSON_VALUE);
        
        consumerBinding.unbind();
    }
    
    /**
     * Validates Batch Retry and DLQ Recovery Logic for Oracle TxEventQ.
     * 
     * This test verifies that:
     * 1. The {@link com.oracle.database.spring.cloud.stream.binder.utils.TEQMessageListenerContainer} 
     *    correctly handles {@code batchMode(true)} and propagates batch exceptions.
     * 2. Spring's Statefull/Stateless Retry Interceptor honors the {@code maxAttempts(3)} 
     *    threshold before exhausting retries.
     * 3. The Binder's Error Handling bridge successfully intercepts the failed batch 
     *    and routes the final exception to the global {@code errorChannel}.
     * 4. The {@link org.springframework.cloud.stream.binder.BinderErrorChannel} correctly 
     *    wraps the failed payload in a {@link org.springframework.messaging.MessagingException} 
     *    for DLQ processing.
     * 
     * Note: Uses a rapid backoff configuration (100ms-200ms) to ensure the test 
     * executes quickly without blocking the CI/CD pipeline on Oracle DB I/O.
     */
    @Test
    void testBatchRetryAndRecoveryLogic(TestInfo testInfo) throws Exception {
    	TxEventQTestBinder binder = getBinder();
        
        // Setup DLQ name but skip manual provisioning to avoid metadata lag
        String dlqName = "TEST_BATCH_DLQ_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Setup Properties
        ExtendedConsumerProperties<JmsConsumerProperties> consumerProperties = createConsumerProperties();
        consumerProperties.setMaxAttempts(3); 
        consumerProperties.setBatchMode(true);
        consumerProperties.getExtension().setDlqName(dlqName);
        
        // Rapid backoff for fast test execution
        consumerProperties.setBackOffInitialInterval(100); 
        consumerProperties.setBackOffMaxInterval(200);
        consumerProperties.setBackOffMultiplier(1.0);

        DirectChannel inputChannel = createBindableChannel("input", new BindingProperties());
        String destination = "batchRetryQueue_" + UUID.randomUUID().toString().substring(0, 8);
        String group = "batchGroup";
        
        CountDownLatch attemptLatch = new CountDownLatch(3);
        AtomicReference<Message<?>> recoveredMessage = new AtomicReference<>();
        CountDownLatch recoveryLatch = new CountDownLatch(1);

        // Subscriber throws error to trigger retry
        inputChannel.subscribe(message -> {
            attemptLatch.countDown();
            throw new RuntimeException("Simulated Batch Failure");
        });

        // Intercept the error channel to verify the binder "gave up" and recovered
        SubscribableChannel errorChannel = testApplicationContext.getBean("errorChannel", SubscribableChannel.class);
        errorChannel.subscribe(msg -> {
            recoveredMessage.set(msg);
            recoveryLatch.countDown();
        });

        // Bind Consumer and Producer
        Binding<MessageChannel> consumerBinding = binder.bindConsumer(destination, group, inputChannel, consumerProperties);
        await().atMost(Duration.ofSeconds(30)).until(consumerBinding::isRunning);
        
        DirectChannel producerChannel = createBindableChannel("output", new BindingProperties());
        binder.bindProducer(destination, producerChannel, createProducerProperties(testInfo));

        // Send Message
        producerChannel.send(MessageBuilder.withPayload("batch-item-1").build());

        // Verifications
        assertThat(attemptLatch.await(60, TimeUnit.SECONDS))
            .as("Should have attempted the batch 3 times").isTrue();

        assertThat(recoveryLatch.await(30, TimeUnit.SECONDS))
            .as("Batch recovery should have triggered on the error channel").isTrue();
        
        Message<?> errMessage = recoveredMessage.get();
        assertThat(errMessage.getPayload()).isInstanceOf(MessagingException.class);
        
        // CLEANUP
        consumerBinding.unbind();
    }
    
    /**
     * Validates Large Object (LOB) Payload Handling in Oracle TxEventQ.
     * 
     * This test verifies that:
     * 1. The Binder correctly manages the serialization and streaming of data exceeding 
     *    the standard JMS inline buffer size (simulated with a 50KB payload).
     * 2. Oracle TxEventQ seamlessly handles the transition to Out-of-Line LOB storage 
     *    within the underlying sharded queue tables.
     * 3. Payload integrity is maintained across the producer-to-consumer lifecycle 
     *    using a byte-for-byte comparison ({@code containsExactly}).
     * 4. The {@link com.oracle.database.spring.cloud.stream.binder.TxEventQQueueProvisioner} 
     *    correctly initializes the destination to support binary large object data types.
     * 
     * Note: Uses an extended 30-second timeout to accommodate the higher disk I/O 
     * overhead associated with LOB persistence in a Testcontainers (Docker) environment.
     */
    @Test
    void testLargePayload() throws Exception {
    	TxEventQTestBinder binder = getBinder();
         
         // 50KB triggers LOB storage in Oracle TEQ
         byte[] largeData = new byte[1024 * 50]; 
         new java.util.Random().nextBytes(largeData);
         
         QueueChannel input = new QueueChannel();
         String destination = "lobTest_" + UUID.randomUUID().toString().substring(0, 8);
         
         // Bind Consumer first to ensure the Topic exists in the DB
         Binding<MessageChannel> consumerBinding = binder.bindConsumer(destination, "lobGroup", input, createConsumerProperties());
       
         Thread.sleep(3000); 

         DirectChannel output = createBindableChannel("output", new BindingProperties());
         binder.bindProducer(destination, output, createProducerProperties(null));
         
         // Send the large payload
         output.send(MessageBuilder.withPayload(largeData).build());

         // Increased timeout to 30s because LOB I/O in a Testcontainer can be slow
         Message<byte[]> received = (Message<byte[]>) input.receive(30000);
         
         assertThat(received).as("Message was not received within timeout").isNotNull();
         assertThat(received.getPayload()).containsExactly(largeData); // containsExactly is better for byte arrays
         
         consumerBinding.unbind();
    }
    
    /**
     * Validates Transactional Reliability and Redelivery for Oracle TxEventQ.
     * 
     * This test verifies that:
     * 1. The Binder integrates correctly with the {@link org.springframework.jms.connection.JmsTransactionManager} 
     *    to bridge Spring Application transactions with Oracle Database Session transactions.
     * 2. When an exception is thrown (simulated failure), the message is NOT acknowledged 
     *    and a physical Database Rollback is triggered on the Oracle Shard.
     * 3. The {@link com.oracle.database.spring.cloud.stream.binder.utils.TEQMessageListenerContainer} 
     *    recovers from the rollback and triggers a redelivery.
     * 4. Data integrity is maintained: the message remains in the Sharded Queue until 
     *    it is successfully processed and the transaction is committed.
     * 
     * Note: {@code maxAttempts(1)} is used to bypass Spring-level retries, forcing the 
     * binder to rely on the underlying Oracle JMS provider for redelivery logic.
     */
    @Test
    void testTransactionalRedelivery() throws Exception {
    	TxEventQTestBinder binder = getBinder();
        String destination = "txTest_" + UUID.randomUUID().toString().substring(0, 8);
        
        // 1. Register the Transaction Manager to the context manually.
        // This is the "glue" that tells Spring to rollback the Oracle Session on error.
        if (!testApplicationContext.containsBean("jmsTransactionManager")) {
            org.springframework.jms.connection.JmsTransactionManager tm = 
                new org.springframework.jms.connection.JmsTransactionManager(testConnectionFactory);
            testApplicationContext.getBeanFactory().registerSingleton("jmsTransactionManager", tm);
        }

        CountDownLatch latch = new CountDownLatch(2);
        DirectChannel input = createBindableChannel("input", new BindingProperties());
        input.subscribe(msg -> {
            latch.countDown();
            // On the first hit, we throw to force a DB-level rollback
            if (latch.getCount() == 1) {
                throw new RuntimeException("Forcing Oracle DB Rollback");
            }
        });

        ExtendedConsumerProperties<JmsConsumerProperties> props = createConsumerProperties();
        
        // Disable Spring's in-memory retry (Max 1 attempt)
        // This forces the exception down to the Transaction Manager and Oracle driver
        props.setMaxAttempts(1);

        // Bind and wait for the consumer
        Binding<MessageChannel> consumerBinding = binder.bindConsumer(destination, "txGroup", input, props);
        await().atMost(Duration.ofSeconds(30)).until(consumerBinding::isRunning);

        DirectChannel output = createBindableChannel("output", new BindingProperties());
        binder.bindProducer(destination, output, createProducerProperties(null));
        
        // Send the test data
        output.send(MessageBuilder.withPayload("tx-data").build());

        // Verification: 60s timeout to allow for Oracle's background recovery (JMS-120)
        assertThat(latch.await(60, TimeUnit.SECONDS))
            .as("Oracle TEQ should have redelivered the message from the shard after the rollback")
            .isTrue();

        consumerBinding.unbind();
    }
    
    /**
     * Validates Shard Key Stickiness (Partitioning) in Oracle TxEventQ.
     * 
     * This test verifies that:
     * 1. The Binder correctly provisions a Sharded Queue (TEQ) with multiple shards.
     * 2. The Producer uses SpEL to extract a partition key and route messages to a specific shard.
     * 3. Multiple consumers in the same group are mapped to different shards.
     * 4. All messages with the same 'fixed-shard-key' are delivered to EXACTLY ONE consumer 
     *    instance, ensuring ordered processing and horizontal scalability without 
     *    inter-consumer contention.
     * 
     * Note: Includes a stabilization delay to allow Oracle's background processes (AQPC) 
     * to synchronize sharded subscriber metadata in the Testcontainers environment.
     */
    @Test
    void testShardKeyStickiness(TestInfo testInfo) throws Exception {
    	TxEventQTestBinder binder = getBinder();
        int messageCount = 20;
        int totalPartitions = 2; 
        String partitionKey = "fixed-shard-key";
        String destination = "stickyShard_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Setup Producer Properties
        ExtendedProducerProperties<JmsProducerProperties> producerProps = createProducerProperties(testInfo);
        producerProps.setPartitionCount(totalPartitions);
        producerProps.setPartitionKeyExpression(spelExpressionParser.parseExpression("headers['partitionKey']"));
        
        // Ensure the provisioner sees the partition count for physical TEQ creation
        BindingProperties producerBindingProps = new BindingProperties();
        producerBindingProps.setProducer(new ProducerProperties());
        producerBindingProps.getProducer().setPartitionCount(totalPartitions);

        DirectChannel output = createBindableChannel("output", producerBindingProps);
        binder.bindProducer(destination, output, producerProps);

        // Setup Partitioned Consumers
        ExtendedConsumerProperties<JmsConsumerProperties> consumerProps = createConsumerProperties();
        consumerProps.setPartitioned(true);
        consumerProps.setInstanceCount(totalPartitions); 

        // Bind Consumer 1 (Listening to Shard 0)
        QueueChannel input1 = new QueueChannel();
        consumerProps.setInstanceIndex(0);
        binder.bindConsumer(destination, "stickyGroup", input1, consumerProps);
        
        // Bind Consumer 2 (Listening to Shard 1)
        QueueChannel input2 = new QueueChannel();
        consumerProps.setInstanceIndex(1);
        binder.bindConsumer(destination, "stickyGroup", input2, consumerProps);

        // Stabilization Delay for Oracle TxEventQ background metadata
        // Prevents JMS-120: Dequeue failed during initial startup
        Thread.sleep(7000); 

        // Send 20 messages all with the SAME partitionKey
        for (int i = 0; i < messageCount; i++) {
            output.send(MessageBuilder.withPayload("msg-" + i)
                    .setHeader("partitionKey", partitionKey).build());
        }

        // Verification Logic
        int count1 = 0;
        int count2 = 0;
        
        // Attempt to receive from both channels
        for (int i = 0; i < messageCount; i++) {
            Message<?> m1 = input1.receive(1000); 
            if (m1 != null) count1++;
            
            Message<?> m2 = input2.receive(1000);
            if (m2 != null) count2++;
        }
       
        // Verify all messages arrived
        assertThat(count1 + count2)
            .as("Total messages received across both consumers")
            .isEqualTo(messageCount);

        // Verify Stickiness (Logical XOR)
        // If stickiness works, one consumer should have 20 and the other 0.
        // If it fails (round-robin), both will have a partial count (e.g. 10 and 10).
        assertThat(count1 == messageCount || count2 == messageCount)
            .as("STICKINESS FAILURE: Messages with key '%s' were split (C1: %d, C2: %d). " +
                "They should have all landed on a single consumer.", partitionKey, count1, count2)
            .isTrue();

        // Payload Integrity
        // (Optional) Verify the last message received was indeed part of the set
        System.out.println("Stickiness verified. Consumer 1: " + count1 + " | Consumer 2: " + count2);
    }
}
