/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.function.springcloudocifunctionsample;

import com.oracle.bmc.functions.requests.InvokeFunctionRequest;
import com.oracle.bmc.functions.responses.InvokeFunctionResponse;
import com.oracle.cloud.spring.function.Function;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("demoapp/api/functions")
@Tag(name = "Function APIs")
public class FunctionController {

    private static final String CONTENT_TYPE = "content-type";

    @Autowired
    Function function;
    @PostMapping(value = "/invoke")
    ResponseEntity<?> invoke(@Parameter(required = true, example = "functionOcid") @RequestParam String functionOcid,
                             @Parameter(required = true, example = "endpoint") @RequestParam String endpoint,
                             @Parameter(required = true, example = "mode") @RequestParam String mode,
                             @io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody String requestBody) {
        String response = "";
        String responseContentType = "";
        try {
            InvokeFunctionRequest.FnInvokeType fnInvokeMode = InvokeFunctionRequest.FnInvokeType.create(mode);
            InvokeFunctionResponse invokeFunctionResponse = function.invokeFunction(functionOcid,
                    endpoint, fnInvokeMode, new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8)));

            if(fnInvokeMode == InvokeFunctionRequest.FnInvokeType.Detached){
                return ResponseEntity.ok().body("Invoked function " + functionOcid +
                        " with opc request id " + invokeFunctionResponse.getOpcRequestId());
            }

            responseContentType = invokeFunctionResponse.getHeaders().get(CONTENT_TYPE).get(0);
            response = new String(invokeFunctionResponse.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body("Error while invoking function" + functionOcid+ ":" + ex.getMessage());
        }
        return ResponseEntity.ok().header(CONTENT_TYPE, responseContentType).body(response);
    }

}
