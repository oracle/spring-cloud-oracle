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

package com.oracle.database.cstream.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.connection.JmsResourceHolder;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.JmsUtils;
import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import oracle.jakarta.jms.AQjmsConsumer;

public class TEQBatchMessageListenerContainer extends DefaultMessageListenerContainer {
    private final MessageListenerContainerResourceFactory transactionalResourceFactory =
            new MessageListenerContainerResourceFactory();

    /**
     * Instance variable to consume from a specific partition
     */
    private int partition = -1;

    /**
     * Instance variable to consume messages in a batch
     */
    private int batchSize = 10;

    /**
     * Getters and setters for partition
     */
    public int getPartition() {
        return this.partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public int getBatchSize() {
        return this.batchSize;
    }

    public void setBatchSize(int bSize) {
        this.batchSize = bSize;
    }

    /**
     * Create a JMS MessageConsumer for the given Session and Destination.
     * <p>This implementation uses JMS 1.1 API.
     * Also sets the corresponding partition on AQjmsConsumer
     *
     * @param session     the JMS Session to create a MessageConsumer for
     * @param destination the JMS Destination to create a MessageConsumer for
     * @return the new JMS MessageConsumer
     * @throws jakarta.jms.JMSException if thrown by JMS API methods
     */
    @Override
    protected MessageConsumer createConsumer(Session session, Destination destination) throws JMSException {
        MessageConsumer consumer = super.createConsumer(session, destination);
        if (this.partition != -1)
            ((AQjmsConsumer) consumer).setPartition(this.partition);
        return consumer;
    }

    protected List<Message> receiveBatch(MessageConsumer consumer, long timeout) throws JMSException {
        List<Message> msgs = new ArrayList<Message>();
        if (timeout > 0) {
            Message[] messages = ((AQjmsConsumer) consumer).bulkReceive(this.batchSize, timeout);
            if (messages == null) return null;
            Collections.addAll(msgs, messages);
        } else if (timeout < 0) {
            Message[] messages = ((AQjmsConsumer) consumer).bulkReceiveNoWait(this.batchSize);
            if (messages == null) return null;
            Collections.addAll(msgs, messages);
        } else {
            Message[] messages = ((AQjmsConsumer) consumer).bulkReceive(this.batchSize);
            if (messages == null) return null;
            Collections.addAll(msgs, messages);
        }
        return msgs;
    }

    // use -> to receive in batch

    /**
     * Actually execute the listener for a message received from the given consumer,
     * fetching all requires resources and invoking the listener.
     *
     * @param session  the JMS Session to work on
     * @param consumer the MessageConsumer to work on
     * @param status   the TransactionStatus (may be {@code null})
     * @return whether a message has been received
     * @throws JMSException if thrown by JMS methods
     * @see #doExecuteListener(jakarta.jms.Session, jakarta.jms.Message)
     */
    @Override
    protected boolean doReceiveAndExecute(Object invoker, @Nullable Session session,
                                          @Nullable MessageConsumer consumer, @Nullable TransactionStatus status) throws JMSException {

        Connection conToClose = null;
        Session sessionToClose = null;
        MessageConsumer consumerToClose = null;
        try {
            Session sessionToUse = session;
            boolean transactional = false;
            if (sessionToUse == null) {
                sessionToUse = ConnectionFactoryUtils.doGetTransactionalSession(
                        obtainConnectionFactory(), this.transactionalResourceFactory, true);
                transactional = (sessionToUse != null);
            }
            if (sessionToUse == null) {
                Connection conToUse;
                if (sharedConnectionEnabled()) {
                    conToUse = getSharedConnection();
                } else {
                    conToUse = createConnection();
                    conToClose = conToUse;
                    conToUse.start();
                }
                sessionToUse = createSession(conToUse);
                sessionToClose = sessionToUse;
            }
            MessageConsumer consumerToUse = consumer;
            if (consumerToUse == null) {
                consumerToUse = createListenerConsumer(sessionToUse);
                consumerToClose = consumerToUse;
            }
            List<Message> messages = receiveBatch(consumer, getReceiveTimeout());
            if (messages != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Received message of type [" + messages.getClass() + "] from consumer [" +
                            consumerToUse + "] of " + (transactional ? "transactional " : "") + "session [" +
                            sessionToUse + "]");
                }
                messageReceived(invoker, sessionToUse);
                boolean exposeResource = (!transactional && isExposeListenerSession() &&
                        !TransactionSynchronizationManager.hasResource(obtainConnectionFactory()));
                if (exposeResource) {
                    TransactionSynchronizationManager.bindResource(
                            obtainConnectionFactory(), new LocallyExposedJmsResourceHolder(sessionToUse));
                }
                try {
                    doExecuteListener(sessionToUse, messages);
                } catch (Throwable ex) {
                    if (status != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Rolling back transaction because of listener exception thrown: " + ex);
                        }
                        status.setRollbackOnly();
                    }
                    handleListenerException(ex);
                    // Rethrow JMSException to indicate an infrastructure problem
                    // that may have to trigger recovery...
                    if (ex instanceof JMSException jmsException) {
                        throw jmsException;
                    }
                } finally {
                    if (exposeResource) {
                        TransactionSynchronizationManager.unbindResource(obtainConnectionFactory());
                    }
                }
                // Indicate that a message has been received.
                return true;
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Consumer [" + consumerToUse + "] of " + (transactional ? "transactional " : "") +
                            "session [" + sessionToUse + "] did not receive a message");
                }
                noMessageReceived(invoker, sessionToUse);
                // Nevertheless call commit, in order to reset the transaction timeout (if any).
                if (shouldCommitAfterNoMessageReceived(sessionToUse)) {
                    super.commitIfNecessary(sessionToUse, null);
                }
                // Indicate that no message has been received.
                return false;
            }
        } finally {
            JmsUtils.closeMessageConsumer(consumerToClose);
            JmsUtils.closeSession(sessionToClose);
            ConnectionFactoryUtils.releaseConnection(conToClose, getConnectionFactory(), true);
        }
    }

    protected void doExecuteListener(Session session, List<Message> messages) throws JMSException {
        if (!isAcceptMessagesWhileStopping() && !isRunning()) {
            if (logger.isWarnEnabled()) {
                logger.warn("Rejecting received messages because of the listener container " +
                        "having been stopped in the meantime: " + messages);
            }
            rollbackIfNecessary(session);
            throw new MessageRejectedWhileStoppingException();
        }

        try {
            invokeListener(session, messages);
        } catch (JMSException | RuntimeException | Error ex) {
            rollbackOnExceptionIfNecessary(session, ex);
            throw ex;
        }
        commitIfNecessary(session, messages);
    }

    protected void commitIfNecessary(Session session, @Nullable List<Message> messages) throws JMSException {
        // Commit session or acknowledge message.
        if (session.getTransacted()) {
            // Commit necessary - but avoid commit call within a JTA transaction.
            if (isSessionLocallyTransacted(session)) {
                // Transacted session created by this container -> commit.
                JmsUtils.commitIfNecessary(session);
            }
        } else if (messages != null && isClientAcknowledge(session)) {
            // acknowledge last message of the group only
            messages.get(messages.size() - 1).acknowledge();
        }
    }

    @Override
    protected boolean isSessionLocallyTransacted(Session session) {
        if (!isSessionTransacted()) {
            return false;
        }
        JmsResourceHolder resourceHolder =
                (JmsResourceHolder) TransactionSynchronizationManager.getResource(obtainConnectionFactory());
        return (resourceHolder == null || resourceHolder instanceof LocallyExposedJmsResourceHolder ||
                !resourceHolder.containsSession(session));
    }

    protected void invokeListener(Session session, List<Message> messages) throws JMSException {
        Object listener = getMessageListener();

        if (listener instanceof TEQBatchMessageListener teqBatchListener) {
            this.doInvokeListener(teqBatchListener, session, messages);
        } else if (listener != null) {
            throw new IllegalArgumentException(
                    "Only TEQBatchMessageListener supported: " + listener);
        } else {
            throw new IllegalStateException("No message listener specified - see property 'messageListener'");
        }
    }

    protected void doInvokeListener(TEQBatchMessageListener listener, Session session, List<Message> messages)
            throws JMSException {

        Connection conToClose = null;
        Session sessionToClose = null;
        try {
            Session sessionToUse = session;
            if (!isExposeListenerSession()) {
                // We need to expose a separate Session.
                conToClose = createConnection();
                sessionToClose = createSession(conToClose);
                sessionToUse = sessionToClose;
            }
            // Actually invoke the message listener...
            listener.setHeaderMapper(new SpecCompliantJmsHeaderMapper());
            listener.onMessage(messages, sessionToUse);
            // Clean up specially exposed Session, if any.
            if (sessionToUse != session) {
                if (sessionToUse.getTransacted() && isSessionLocallyTransacted(sessionToUse)) {
                    // Transacted session created by this container -> commit.
                    JmsUtils.commitIfNecessary(sessionToUse);
                }
            }
        } finally {
            JmsUtils.closeSession(sessionToClose);
            JmsUtils.closeConnection(conToClose);
        }
    }

    /**
     * ResourceFactory implementation that delegates to this listener container's protected callback methods.
     */
    private class MessageListenerContainerResourceFactory implements ConnectionFactoryUtils.ResourceFactory {

        @Override
        @Nullable
        public Connection getConnection(JmsResourceHolder holder) {
            return TEQBatchMessageListenerContainer.this.getConnection(holder);
        }

        @Override
        @Nullable
        public Session getSession(JmsResourceHolder holder) {
            return TEQBatchMessageListenerContainer.this.getSession(holder);
        }

        @Override
        public Connection createConnection() throws JMSException {
            if (TEQBatchMessageListenerContainer.this.sharedConnectionEnabled()) {
                Connection sharedCon = TEQBatchMessageListenerContainer.this.getSharedConnection();
                return new SingleConnectionFactory(sharedCon).createConnection();
            } else {
                return TEQBatchMessageListenerContainer.this.createConnection();
            }
        }

        @Override
        public Session createSession(Connection con) throws JMSException {
            return TEQBatchMessageListenerContainer.this.createSession(con);
        }

        @Override
        public boolean isSynchedLocalTransactionAllowed() {
            return TEQBatchMessageListenerContainer.this.isSessionTransacted();
        }
    }

    @SuppressWarnings("serial")
    private static class MessageRejectedWhileStoppingException extends RuntimeException {
    }
}


class LocallyExposedJmsResourceHolder extends JmsResourceHolder {

    public LocallyExposedJmsResourceHolder(Session session) {
        super(session);
    }
}
