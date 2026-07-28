/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.function.springcloudocifunctionsample;

import com.oracle.bmc.functions.requests.InvokeFunctionRequest;
import com.oracle.bmc.functions.responses.InvokeFunctionResponse;
import com.oracle.cloud.spring.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("demoapp/api/functions")
public class FunctionController {

    private static final String CONTENT_TYPE = "content-type";

    @Autowired
    Function function;
    @PostMapping(value = "/invoke")
    ResponseEntity<?> invoke(@RequestParam String functionOcid,
                             @RequestParam String mode,
                             @RequestBody String requestBody) {
        String response = "";
        String responseContentType = "";
        try {
            InvokeFunctionRequest.FnInvokeType fnInvokeMode = InvokeFunctionRequest.FnInvokeType.create(mode);
            InvokeFunctionResponse invokeFunctionResponse = function.invokeFunction(functionOcid,
                    fnInvokeMode, new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8)));

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
