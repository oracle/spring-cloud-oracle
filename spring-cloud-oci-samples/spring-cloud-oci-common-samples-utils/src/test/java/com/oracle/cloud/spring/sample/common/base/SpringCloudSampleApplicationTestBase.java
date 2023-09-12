/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.common.base;



import com.oracle.cloud.spring.sample.common.util.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;

/**
 * Base class to be extended by Spring boot test classes
 */

public class SpringCloudSampleApplicationTestBase {

	static final String PRIVATE_KEY_PATH = "privateKey";
	static final String PRIVATE_KEY_CONTENT = "privateKeyContent";
	static final String privateKeyFilePath = System.getProperty("user.home") + File.separator + "privatekey.pem";
	static final String privateKeyContent = System.getProperty(PRIVATE_KEY_CONTENT) != null ? System.getProperty(PRIVATE_KEY_CONTENT) :
			System.getenv().get(PRIVATE_KEY_CONTENT);
	@BeforeAll
	static void beforeAll() throws Exception {
		System.setProperty(PRIVATE_KEY_PATH, privateKeyFilePath);
		FileUtils.createFile(privateKeyFilePath, privateKeyContent.replace("\\n", "\n"));
	}

	@AfterAll
	static void AfterAll() {
		FileUtils.deleteFile(privateKeyFilePath);
	}
}
