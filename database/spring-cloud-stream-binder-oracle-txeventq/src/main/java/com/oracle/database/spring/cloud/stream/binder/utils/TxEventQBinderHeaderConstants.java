package com.oracle.database.spring.cloud.stream.binder.utils;

public class TxEventQBinderHeaderConstants {
    public static final String MESSAGE_CONNECTION = "oracle.jdbc.internal.connection";
    public static final String CONNECTION_CONSUMER = "oracle.jdbc.internal.callback";
    public static final String MESSAGE_CONTEXT = "oracle.jdbc.internal.message_context";

    private TxEventQBinderHeaderConstants() {
    }
}
