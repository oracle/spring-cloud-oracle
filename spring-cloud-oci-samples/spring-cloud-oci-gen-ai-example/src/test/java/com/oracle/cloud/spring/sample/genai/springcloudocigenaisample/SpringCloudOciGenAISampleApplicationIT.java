package com.oracle.cloud.spring.sample.genai.springcloudocigenaisample;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OCI_COMPARTMENT_ID", matches = ".+")
@TestPropertySource(locations="classpath:application-test.yaml")
public class SpringCloudOciGenAISampleApplicationIT {
}
