/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.function;

import com.oracle.bmc.functions.FunctionsInvokeClient;
import com.oracle.bmc.functions.requests.InvokeFunctionRequest;
import com.oracle.bmc.functions.responses.InvokeFunctionResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FunctionImplTests {

    final FunctionsInvokeClient fnInvokeClient = mock(FunctionsInvokeClient.class);
    final Function function = new FunctionImpl(fnInvokeClient);

    @Test
    void testFunctionClient() throws Exception {
        when(fnInvokeClient.invokeFunction(any())).thenReturn(mock(InvokeFunctionResponse.class));
        assertNotNull(function.invokeFunction("fn_ocid", "fn_endpoint",
                InvokeFunctionRequest.FnInvokeType.Detached,
                new ByteArrayInputStream("fn_request".getBytes(StandardCharsets.UTF_8))));
        assertNotNull(function.getFunctionsInvokeClient());

    }
}
