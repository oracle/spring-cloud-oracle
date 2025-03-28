// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.jsonevents;

import java.io.File;
import java.time.Duration;
import java.util.List;

import com.oracle.database.spring.jsonevents.model.Sensor;
import com.oracle.database.spring.jsonevents.model.SensorEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.MountableFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

@Testcontainers
@SpringBootTest
@Sql("/init.sql") // Initialize the app tables
public class JSONEventsSampleTest {
    /**
     * The Testcontainers Oracle Free module let's us create an Oracle database container in a junit context.
     */
    @Container
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.7-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withUsername("testuser")
            .withPassword("testpwd");

    /**
     * Dynamically configure Spring Boot properties to use the Testcontainers database.
     */
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // Configure the Kafka Java Client for Oracle TxEventQ
        String bootstrapServers = "localhost:" + oracleContainer.getOraclePort();
        String propsPath = new File("src/main/resources").getAbsolutePath();
        registry.add("app.bootstrapServers", () -> bootstrapServers);
        registry.add("app.ojdbcPath", () -> propsPath);
        // Configure for Spring Data JDBC
        registry.add("JDBC_URL", oracleContainer::getJdbcUrl);
        registry.add("USERNAME", oracleContainer::getUsername);
        registry.add("PASSWORD", oracleContainer::getPassword);
    }

    @BeforeAll
    public static void setUp() throws Exception {
        // Run the okafka.sql grants as sysdba on the database test container
        oracleContainer.start();
        oracleContainer.copyFileToContainer(MountableFile.forClasspathResource("txeventq.sql"), "/tmp/txeventq.sql");
        oracleContainer.execInContainer("sqlplus", "sys / as sysdba", "@/tmp/txeventq.sql");
    }

    @Autowired
    SensorController sensorController;

    @Autowired
    OKafkaComponent okafkaComponent;


    @Test
    void jsonEventsSampleAppTest() throws InterruptedException {
        // Produce events for two different stations
        System.out.println("Produced events for ST001");
        sensorController.produce(event1());
        System.out.println("Produced events for ST002");
        sensorController.produce(event2());

        System.out.println("Waiting for consumer to process all events");
        // Wait for queues to process all events
        okafkaComponent.await();

        // Assert all events have been processed and are available in the database
        ResponseEntity<List<Sensor>> st001Events = sensorController.getEvents("ST001");
        ResponseEntity<List<Sensor>> st002Events = sensorController.getEvents("ST002");

        System.out.printf("Received %d events for ST001\n", st001Events.getBody().size());
        assertEquals(st001Events.getBody().size(), 5);
        System.out.printf("Received %d events for ST002\n", st002Events.getBody().size());
        assertEquals(st002Events.getBody().size(), 10);

    }

    private SensorEvent event1() {
        SensorEvent event = new SensorEvent();
        event.setData(List.of(
                "ST001,65.32,22.5,3.8",
                "ST001,78.90,18.3,2.5",
                "ST001,42.15,28.7,6.2",
                "ST001,55.67,25.1,4.7",
                "ST001,70.45,20.9,1.9"
        ));
        return event;
    }

    private SensorEvent event2() {
        SensorEvent event = new SensorEvent();
        event.setData(List.of(
                "ST002,68.75,23.4,4.2",
                "ST002,70.20,22.8,3.9",
                "ST002,65.90,24.1,4.5",
                "ST002,72.30,21.5,3.6",
                "ST002,69.45,22.3,4.0",
                "ST002,67.80,23.7,4.3",
                "ST002,71.60,21.9,3.8",
                "ST002,66.25,24.5,4.7",
                "ST002,73.10,20.8,3.4",
                "ST002,68.95,22.6,4.1"
        ));
        return event;
    }
}
