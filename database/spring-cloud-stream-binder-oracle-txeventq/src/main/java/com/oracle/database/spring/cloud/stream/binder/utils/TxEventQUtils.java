package com.oracle.database.spring.cloud.stream.binder.utils;

import java.sql.Connection;
import java.util.function.Consumer;

import org.springframework.messaging.Message;

public class TxEventQUtils {
	/* Private constructor to avoid creating object of class TxEventQUtils */
	private TxEventQUtils() { }

	/* Static Utility Methods */
	public static Connection getDBConnection(Message<?> message) {
		return (Connection) message
								.getHeaders()
								.getOrDefault(TxEventQBinderHeaderConstants.MESSAGE_CONNECTION, null);
	}

	public static <T> Message<T> setConnectionCallbackContext(Message<T> message,
			Consumer<Connection> callback,
			Message<?> oldMessage) {
		return TxEventQMessageBuilder
				.fromMessage(message)
				.setConnectionCallbackContext(callback, oldMessage)
				.build();
	}

	public static <T> Message<T> setConnectionCallback(Message<T> message, Consumer<Connection> callback) {
		return TxEventQMessageBuilder
				.fromMessage(message)
				.setConnectionCallback(callback)
				.build();
	}
}
