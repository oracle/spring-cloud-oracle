/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.function;

import com.oracle.bmc.functions.FunctionsInvoke;
import com.oracle.bmc.functions.requests.InvokeFunctionRequest;
import com.oracle.bmc.functions.responses.InvokeFunctionResponse;

import java.io.InputStream;

/**
 * Interface for defining the OCI Function module.
 */
public interface Function {

    /**
     * Direct instance of OCI Function Invoke Client.
     * @return FunctionsInvoke
     */
    FunctionsInvoke getFunctionsInvokeClient();

    /**
     * Invoke an OCI Function
     * @param functionOcid OCID of the Function
     * @param mode Function invocation mode. Allowed values are as per {@link InvokeFunctionRequest.FnInvokeType}
     * @param request Request body as String
     * @return InvokeFunctionResponse
     *
     */
    InvokeFunctionResponse invokeFunction(String functionOcid, InvokeFunctionRequest.FnInvokeType mode, InputStream request) throws Exception;

    /**
     * Invoke an OCI Function.
     * The endpoint parameter is ignored and the invoke endpoint is resolved from OCI for the supplied function OCID.
     * @param functionOcid OCID of the Function
     * @param endpoint Ignored. The invoke endpoint is resolved from OCI metadata.
     * @param mode Function invocation mode. Allowed values are as per {@link InvokeFunctionRequest.FnInvokeType}
     * @param request Request body as String
     * @return InvokeFunctionResponse
     *
     */
    InvokeFunctionResponse invokeFunction(String functionOcid, String endpoint, InvokeFunctionRequest.FnInvokeType mode, InputStream request) throws Exception;
}
