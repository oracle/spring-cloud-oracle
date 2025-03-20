package com.oracle.database.spring.cloud.stream.binder.utils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public final class TxEventQMessageBuilder<T> extends AbstractIntegrationMessageBuilder<T> {

	private static final Log LOGGER = LogFactory.getLog(TxEventQMessageBuilder.class);

	private final T payload;

	private final IntegrationMessageHeaderAccessor headerAccessor;

	@Nullable
	private final Message<T> originalMessage;

	private volatile boolean modified;

	private String[] readOnlyHeaders;

	private TxEventQMessageBuilder(T payload, @Nullable Message<T> originalMessage) {
		Assert.notNull(payload, "payload must not be null");
		this.payload = payload;
		this.originalMessage = originalMessage;
		this.headerAccessor = new IntegrationMessageHeaderAccessor(originalMessage);
		if (originalMessage != null) {
			this.modified = (!this.payload.equals(originalMessage.getPayload()));
		}
	}

	@Override
	public T getPayload() {
		return this.payload;
	}

	@Override
	public Map<String, Object> getHeaders() {
		return this.headerAccessor.toMap();
	}

	@Nullable
	@Override
	public <V> V getHeader(String key, Class<V> type) {
		return this.headerAccessor.getHeader(key, type);
	}

	public static <T> TxEventQMessageBuilder<T> fromMessage(Message<T> message) {
		Assert.notNull(message, "message must not be null");
		return new TxEventQMessageBuilder<>(message.getPayload(), message);
	}

	public static <T> TxEventQMessageBuilder<T> withPayload(T payload) {
		return new TxEventQMessageBuilder<>(payload, null);
	}

	@Override
	public TxEventQMessageBuilder<T> setHeader(String headerName, @Nullable Object headerValue) {
		this.headerAccessor.setHeader(headerName, headerValue);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> setHeaderIfAbsent(String headerName, Object headerValue) {
		this.headerAccessor.setHeaderIfAbsent(headerName, headerValue);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> removeHeaders(String... headerPatterns) {
		this.headerAccessor.removeHeaders(headerPatterns);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> removeHeader(String headerName) {
		if (!this.headerAccessor.isReadOnly(headerName)) {
			this.headerAccessor.removeHeader(headerName);
		}
		else if (LOGGER.isInfoEnabled()) {
			LOGGER.info("The header [" + headerName + "] is ignored for removal because it is is readOnly.");
		}
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> copyHeaders(@Nullable Map<String, ?> headersToCopy) {
		this.headerAccessor.copyHeaders(headersToCopy);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> copyHeadersIfAbsent(@Nullable Map<String, ?> headersToCopy) {
		if (headersToCopy != null) {
			for (Map.Entry<String, ?> entry : headersToCopy.entrySet()) {
				String headerName = entry.getKey();
				if (!this.headerAccessor.isReadOnly(headerName)) {
					this.headerAccessor.setHeaderIfAbsent(headerName, entry.getValue());
				}
			}
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	protected List<List<Object>> getSequenceDetails() {
		return (List<List<Object>>) this.headerAccessor.getHeader(IntegrationMessageHeaderAccessor.SEQUENCE_DETAILS);
	}

	@Override
	@Nullable
	protected Object getCorrelationId() {
		return this.headerAccessor.getCorrelationId();
	}

	@Override
	protected Object getSequenceNumber() {
		return this.headerAccessor.getSequenceNumber();
	}

	@Override
	protected Object getSequenceSize() {
		return this.headerAccessor.getSequenceSize();
	}

	@Override
	public TxEventQMessageBuilder<T> pushSequenceDetails(Object correlationId, int sequenceNumber, int sequenceSize) {
		super.pushSequenceDetails(correlationId, sequenceNumber, sequenceSize);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> popSequenceDetails() {
		super.popSequenceDetails();
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> setExpirationDate(@Nullable Long expirationDate) {
		super.setExpirationDate(expirationDate);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> setExpirationDate(@Nullable Date expirationDate) {
		super.setExpirationDate(expirationDate);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> setCorrelationId(Object correlationId) {
		super.setCorrelationId(correlationId);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> setReplyChannel(MessageChannel replyChannel) {
		super.setReplyChannel(replyChannel);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> setReplyChannelName(String replyChannelName) {
		super.setReplyChannelName(replyChannelName);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> setErrorChannel(MessageChannel errorChannel) {
		super.setErrorChannel(errorChannel);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> setErrorChannelName(String errorChannelName) {
		super.setErrorChannelName(errorChannelName);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> setSequenceNumber(Integer sequenceNumber) {
		super.setSequenceNumber(sequenceNumber);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> setSequenceSize(Integer sequenceSize) {
		super.setSequenceSize(sequenceSize);
		return this;
	}

	@Override
	public TxEventQMessageBuilder<T> setPriority(Integer priority) {
		super.setPriority(priority);
		return this;
	}

	public TxEventQMessageBuilder<T> setConnectionCallback(Consumer<Connection> callback) {
		this.setHeader(TxEventQBinderHeaderConstants.CONNECTION_CONSUMER, callback);
		return this;
	}

	public TxEventQMessageBuilder<T> setConnectionCallbackContext(Consumer<Connection> callback, Message<?> message) {
		this.setHeader(TxEventQBinderHeaderConstants.CONNECTION_CONSUMER, callback);
		this.setHeader(TxEventQBinderHeaderConstants.MESSAGE_CONTEXT, TxEventQUtils.getDBConnection(message));
		return this;
	}

	public TxEventQMessageBuilder<T> readOnlyHeaders(String... readOnlyHeaders) {
		this.readOnlyHeaders = readOnlyHeaders != null ? Arrays.copyOf(readOnlyHeaders, readOnlyHeaders.length) : null;
		this.headerAccessor.setReadOnlyHeaders(readOnlyHeaders);
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Message<T> build() {
		if (!this.modified && !this.headerAccessor.isModified() && this.originalMessage != null
				&& !containsReadOnly(this.originalMessage.getHeaders())) {
			return this.originalMessage;
		}
		if (this.payload instanceof Throwable) {
			return (Message<T>) new ErrorMessage((Throwable) this.payload, this.headerAccessor.toMap());
		}
		return new GenericMessage<>(this.payload, this.headerAccessor.toMap());
	}

	private boolean containsReadOnly(MessageHeaders headers) {
		if (!ObjectUtils.isEmpty(this.readOnlyHeaders)) {
			for (String readOnly : this.readOnlyHeaders) {
				if (headers.containsKey(readOnly)) {
					return true;
				}
			}
		}
		return false;
	}

}
