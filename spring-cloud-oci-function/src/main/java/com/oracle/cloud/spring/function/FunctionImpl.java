/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.function;

import com.oracle.bmc.functions.FunctionsInvoke;
import com.oracle.bmc.functions.requests.InvokeFunctionRequest;
import com.oracle.bmc.functions.responses.InvokeFunctionResponse;
import com.oracle.bmc.util.StreamUtils;

import java.io.InputStream;

/**
 * Implementation of the OCI Function module.
 */
public class FunctionImpl implements Function{

    FunctionsInvoke functionsInvokeClient;

    public FunctionImpl(FunctionsInvoke functionsInvokeClient) {
        this.functionsInvokeClient = functionsInvokeClient;
    }

    /**
     * Direct instance of OCI Function Invoke Client.
     * @return FunctionsInvoke
     */
    @Override
    public FunctionsInvoke getFunctionsInvokeClient() {
        return this.functionsInvokeClient;
    }

    /**
     * Invoke an OCI Function
     * @param functionOcid OCID of the Function
     * @param endpoint Function endpoint
     * @param mode Function invocation mode. Allowed values are as per {@link InvokeFunctionRequest.FnInvokeType}
     * @param request Request body as InputStream
     * @return InvokeFunctionResponse
     *
     */
    @Override
    public InvokeFunctionResponse invokeFunction(String functionOcid, String endpoint,
                                                 InvokeFunctionRequest.FnInvokeType mode, InputStream request) throws Exception {
        System.out.println("Invoking function " + functionOcid + " for endpoint " + endpoint + ", with payload : " + request);
        functionsInvokeClient.setEndpoint(endpoint);
        InvokeFunctionResponse response = null;
        InvokeFunctionRequest invokeFunctionRequest = InvokeFunctionRequest.builder()
                .functionId(functionOcid)
                .invokeFunctionBody(StreamUtils.createByteArrayInputStream(request.readAllBytes()))
                .fnInvokeType(mode).build();

        response = functionsInvokeClient.invokeFunction(invokeFunctionRequest);
        System.out.println("Function " + functionOcid + " invocation successful with Request ID " + response.getOpcRequestId());

        return response;
    }

}
