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
import com.oracle.bmc.util.StreamUtils;

import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Implementation of the OCI Function module.
 */
public class FunctionImpl implements Function{

    private final FunctionsInvoke functionsInvokeClient;
    private final FunctionsManagement functionsManagementClient;
    private final Supplier<FunctionsInvoke> functionsInvokeClientSupplier;

    public FunctionImpl(FunctionsInvoke functionsInvokeClient, FunctionsManagement functionsManagementClient,
                        Supplier<FunctionsInvoke> functionsInvokeClientSupplier) {
        this.functionsInvokeClient = functionsInvokeClient;
        this.functionsManagementClient = functionsManagementClient;
        this.functionsInvokeClientSupplier = functionsInvokeClientSupplier;
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
     * @param mode Function invocation mode. Allowed values are as per {@link InvokeFunctionRequest.FnInvokeType}
     * @param request Request body as InputStream
     * @return InvokeFunctionResponse
     *
     */
    @Override
    public InvokeFunctionResponse invokeFunction(String functionOcid,
                                                 InvokeFunctionRequest.FnInvokeType mode, InputStream request) throws Exception {
        String endpoint = resolveInvokeEndpoint(functionOcid);
        System.out.println("Invoking function " + functionOcid + " for endpoint " + endpoint + ", with payload : " + request);
        InvokeFunctionRequest invokeFunctionRequest = InvokeFunctionRequest.builder()
                .functionId(functionOcid)
                .invokeFunctionBody(StreamUtils.createByteArrayInputStream(request.readAllBytes()))
                .fnInvokeType(mode).build();

        InvokeFunctionResponse response;
        try (FunctionsInvoke invokeClient = functionsInvokeClientSupplier.get()) {
            invokeClient.setEndpoint(endpoint);
            response = invokeClient.invokeFunction(invokeFunctionRequest);
        }
        System.out.println("Function " + functionOcid + " invocation successful with Request ID " + response.getOpcRequestId());

        return response;
    }

    @Override
    public InvokeFunctionResponse invokeFunction(String functionOcid, String endpoint,
                                                 InvokeFunctionRequest.FnInvokeType mode, InputStream request) throws Exception {
        return invokeFunction(functionOcid, mode, request);
    }

    private String resolveInvokeEndpoint(String functionOcid) {
        GetFunctionResponse functionResponse = functionsManagementClient.getFunction(GetFunctionRequest.builder()
                .functionId(functionOcid)
                .build());
        com.oracle.bmc.functions.model.Function function = functionResponse.getFunction();
        if (function == null || function.getInvokeEndpoint() == null || function.getInvokeEndpoint().isBlank()) {
            throw new IllegalStateException("No invoke endpoint found for function " + functionOcid);
        }
        return function.getInvokeEndpoint();
    }
}
