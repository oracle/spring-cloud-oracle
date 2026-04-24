/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.function;

import com.oracle.bmc.functions.FunctionsManagement;
import com.oracle.bmc.functions.FunctionsInvoke;
import com.oracle.bmc.functions.requests.GetFunctionRequest;
import com.oracle.bmc.functions.requests.InvokeFunctionRequest;
import com.oracle.bmc.functions.responses.GetFunctionResponse;
import com.oracle.bmc.functions.responses.InvokeFunctionResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FunctionImplTests {

    @Test
    void testFunctionClientResolvesRegisteredEndpoint() throws Exception {
        AtomicReference<String> resolvedFunctionId = new AtomicReference<>();
        AtomicReference<String> invokedEndpoint = new AtomicReference<>();
        AtomicReference<InvokeFunctionRequest> capturedRequest = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean(false);
        InvokeFunctionResponse invokeFunctionResponse = InvokeFunctionResponse.builder()
                .opcRequestId("opc-request-id")
                .headers(Map.of())
                .build();

        FunctionsInvoke sharedClient = functionsInvokeClient(null, null, null, null);
        FunctionsInvoke perInvocationClient = functionsInvokeClient(invokedEndpoint, capturedRequest, closed, invokeFunctionResponse);
        FunctionsManagement functionsManagementClient = functionsManagementClient(resolvedFunctionId, "https://registered.endpoint");
        Supplier<FunctionsInvoke> functionsInvokeClientSupplier = () -> perInvocationClient;
        Function function = new FunctionImpl(sharedClient, functionsManagementClient, functionsInvokeClientSupplier);

        InvokeFunctionResponse response = function.invokeFunction("fn_ocid", "https://attacker.endpoint",
                InvokeFunctionRequest.FnInvokeType.Detached,
                new ByteArrayInputStream("fn_request".getBytes(StandardCharsets.UTF_8)));

        assertSame(invokeFunctionResponse, response);
        assertSame(sharedClient, function.getFunctionsInvokeClient());
        assertEquals("fn_ocid", resolvedFunctionId.get());
        assertEquals("https://registered.endpoint", invokedEndpoint.get());
        assertNotNull(capturedRequest.get());
        assertEquals("fn_ocid", capturedRequest.get().getFunctionId());
        assertEquals(InvokeFunctionRequest.FnInvokeType.Detached, capturedRequest.get().getFnInvokeType());
        assertEquals("fn_request", new String(capturedRequest.get().getInvokeFunctionBody().readAllBytes(), StandardCharsets.UTF_8));
        assertEquals(true, closed.get());
    }

    @Test
    void testFunctionClientRejectsFunctionsWithoutInvokeEndpoint() {
        FunctionsInvoke sharedClient = functionsInvokeClient(null, null, null, null);
        FunctionsInvoke perInvocationClient = functionsInvokeClient(null, null, null, null);
        FunctionsManagement functionsManagementClient = functionsManagementClient(new AtomicReference<>(), null);
        Function function = new FunctionImpl(sharedClient, functionsManagementClient, () -> perInvocationClient);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> function.invokeFunction("fn_ocid", InvokeFunctionRequest.FnInvokeType.Sync,
                        new ByteArrayInputStream("fn_request".getBytes(StandardCharsets.UTF_8))));

        assertEquals("No invoke endpoint found for function fn_ocid", exception.getMessage());
    }

    private FunctionsManagement functionsManagementClient(AtomicReference<String> resolvedFunctionId, String invokeEndpoint) {
        return (FunctionsManagement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{FunctionsManagement.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getFunction" -> {
                        GetFunctionRequest request = (GetFunctionRequest) args[0];
                        resolvedFunctionId.set(request.getFunctionId());
                        yield GetFunctionResponse.builder()
                                .function(com.oracle.bmc.functions.model.Function.builder().invokeEndpoint(invokeEndpoint).build())
                                .build();
                    }
                    case "setRegion", "setEndpoint", "refreshClient", "useRealmSpecificEndpointTemplate", "close" -> null;
                    case "getEndpoint" -> null;
                    case "getPaginators", "getWaiters" -> null;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    private FunctionsInvoke functionsInvokeClient(AtomicReference<String> invokedEndpoint,
                                                  AtomicReference<InvokeFunctionRequest> capturedRequest,
                                                  AtomicBoolean closed,
                                                  InvokeFunctionResponse invokeFunctionResponse) {
        return (FunctionsInvoke) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{FunctionsInvoke.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "setEndpoint" -> {
                        if (invokedEndpoint != null) {
                            invokedEndpoint.set((String) args[0]);
                        }
                        yield null;
                    }
                    case "invokeFunction" -> {
                        if (capturedRequest != null) {
                            capturedRequest.set((InvokeFunctionRequest) args[0]);
                        }
                        yield invokeFunctionResponse;
                    }
                    case "close" -> {
                        if (closed != null) {
                            closed.set(true);
                        }
                        yield null;
                    }
                    case "setRegion" -> null;
                    case "refreshClient", "useRealmSpecificEndpointTemplate" -> null;
                    case "getEndpoint" -> invokedEndpoint == null ? null : invokedEndpoint.get();
                    case "getWaiters", "getPaginators" -> null;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }
}
