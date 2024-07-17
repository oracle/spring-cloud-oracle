/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.function.springcloudocifunctionsample;

import com.oracle.bmc.functions.requests.InvokeFunctionRequest;
import com.oracle.bmc.functions.responses.InvokeFunctionResponse;
import com.oracle.cloud.spring.function.Function;
import com.oracle.cloud.spring.sample.common.base.SpringCloudSampleApplicationTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Environment variables needed to run this tests are :
 * all variables in application-test.properties files,
 * functionOcid,
 * endpoint,
 * request
 */
@SpringBootTest
@EnabledIfSystemProperty(named = "it.function", matches = "true")
@TestPropertySource(locations="classpath:application-test.properties")
class SpringCloudOciFunctionSampleApplicationTests extends SpringCloudSampleApplicationTestBase {
	@Autowired
	Function function;

	@Value("${functionOcid}")
	String functionOcid;

	@Value("${endpoint}")
	String endpoint;

	@Value("${request}")
	String request;

	@Test
	void testAsyncFunctionInvoke() throws Exception {
		InvokeFunctionResponse invokeFunctionResponse = function.invokeFunction(functionOcid, endpoint, InvokeFunctionRequest.FnInvokeType.Detached, new ByteArrayInputStream(request.getBytes()));
		assertNotNull(invokeFunctionResponse.getOpcRequestId());
	}

	@Test
	void testSyncFunctionInvoke() throws Exception {
		InvokeFunctionResponse invokeFunctionResponse = function.invokeFunction(functionOcid, endpoint, InvokeFunctionRequest.FnInvokeType.Sync, new ByteArrayInputStream(request.getBytes()));
		InputStream inputStream = invokeFunctionResponse.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String temp = "";
		StringBuilder sb = new StringBuilder();
		while((temp = br.readLine()) != null) {
			sb.append(temp);
		}
		br.close();
		System.out.println("Result = "+sb);
		assertNotNull(sb.toString());
	}

}
