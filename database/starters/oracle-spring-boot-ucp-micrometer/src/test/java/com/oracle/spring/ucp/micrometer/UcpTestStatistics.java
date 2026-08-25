// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.ucp.micrometer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oracle.ucp.UniversalConnectionPoolStatistics;

final class UcpTestStatistics {

    private final Map<String, Object> values = new HashMap<>();
    private final Set<String> unsupportedMethods = new HashSet<>();
    private final UniversalConnectionPoolStatistics statistics = createStatistics();

    UniversalConnectionPoolStatistics statistics() {
        return statistics;
    }

    void set(String methodName, Object value) {
        values.put(methodName, value);
    }

    void unsupported(String methodName) {
        unsupportedMethods.add(methodName);
    }

    private UniversalConnectionPoolStatistics createStatistics() {
        InvocationHandler handler = (proxy, method, args) -> value(method, proxy, args);
        return (UniversalConnectionPoolStatistics) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { UniversalConnectionPoolStatistics.class }, handler);
    }

    private Object value(Method method, Object proxy, Object[] args) {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "UCP test statistics";
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
        if (unsupportedMethods.contains(method.getName())) {
            throw new NoSuchMethodError("not implemented");
        }
        Object value = values.get(method.getName());
        return (value != null) ? value : defaultValue(method.getReturnType());
    }

    private Object defaultValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == double.class) {
            return 0D;
        }
        return null;
    }
}
