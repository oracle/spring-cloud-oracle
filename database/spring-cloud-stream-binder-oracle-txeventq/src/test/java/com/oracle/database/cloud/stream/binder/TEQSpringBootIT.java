package com.oracle.database.cstream;

import java.sql.SQLException;

import com.oracle.database.cstream.JMSMessageChannelBinder;
import com.oracle.database.cstream.config.TxEventQJmsConfiguration;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

@Testcontainers
@SpringBootTest(classes = TxEventQJmsConfiguration.class)
@Import(TEQSpringBootIT.Config.class)
public class TEQSpringBootIT {
    @Container
    private static final OracleContainer oracleContainer = Util.oracleContainer();

    @Configuration
    public static class Config {
        @Bean
        public PoolDataSource poolDataSource() throws SQLException {
            PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
            Util.configurePoolDataSource(poolDataSource, oracleContainer);
            return poolDataSource;
        }
    }

    @BeforeAll
    public static void setUp() throws Exception {
        Util.startOracleContainer(oracleContainer);
    }

    @Autowired
    JMSMessageChannelBinder jmsMessageChannelBinder;

    @Test
    void contextLoads() {}
}
