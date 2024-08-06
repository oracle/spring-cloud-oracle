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

package com.oracle.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.springframework.cloud.stream.binder.Spy;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;

import com.oracle.cstream.JMSMessageChannelBinder;
import com.oracle.cstream.TxEventQQueueProvisioner;
import com.oracle.cstream.config.JmsConsumerProperties;
import com.oracle.cstream.config.JmsProducerProperties;
import com.oracle.cstream.plsql.OracleDBUtils;
import com.oracle.cstream.utils.Base64UrlNamingStrategy;
import com.oracle.cstream.utils.DestinationNameResolver;
import com.oracle.cstream.utils.JmsMessageDrivenChannelAdapterFactory;
import com.oracle.cstream.utils.JmsSendingMessageHandlerFactory;
import com.oracle.cstream.utils.ListenerContainerFactory;
import com.oracle.cstream.utils.MessageRecoverer;
import com.oracle.cstream.utils.RepublishMessageRecoverer;
import com.oracle.cstream.utils.SpecCompliantJmsHeaderMapper;

import jakarta.jms.ConnectionFactory;
import nativetests.TestObject;
import oracle.jakarta.jms.AQjmsFactory;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

@SuppressWarnings("unchecked")
@Testcontainers
public class TEQPartitionIT extends
        PartitionCapableBinderTests<TxEventQTestBinder, ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>> {

    private static TxEventQTestBinder teqBinder;

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
        TxEventQQueueProvisioner queueProvisioner = new TxEventQQueueProvisioner(connectionFactory, dbutils);

        DB_VERSION = dbversion;
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();
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
        Message<?> result = receive(channel);

        for (int i = 0; i < 5; i++) {
            if (result != null)
                return result;
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = receive(channel);
        }

        return result;
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

}
