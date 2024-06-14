package com.oracle.cloud.spring.sample.genai.springcloudocigenaisample;

import com.oracle.cloud.spring.genai.EmbeddingModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OCI_COMPARTMENT_ID", matches = ".+")
@TestPropertySource(locations="classpath:application-embedding.yaml")
public class SpringCloudGenAiEmbeddingIT {
    @Autowired
    EmbeddingModel embeddingModel;

    @Test
    public void t() {
        Assertions.assertEquals(1, 1);
    }
}
