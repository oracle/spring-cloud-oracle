package com.oracle.spring.json.duality;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import com.oracle.spring.json.duality.builder.DualityViewScanner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

@SpringBootTest
@Testcontainers
public class SpringBootDualityTest {
    public static String readViewFile(String fileName) {
        try {
            File file = new ClassPathResource(Path.of("views", fileName).toString()).getFile();
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Use a containerized Oracle Database instance for testing.
     */
    @Container
    @ServiceConnection
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.6-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(5))
            .withUsername("testuser")
            .withPassword("testpwd");

    @Autowired
    private DualityViewScanner dualityViewScanner;

    @Test
    void contextLoads() {}
}
