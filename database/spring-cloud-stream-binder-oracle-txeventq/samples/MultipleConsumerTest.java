package com.oracle.cstream;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MultipleConsumerTest {
	int y = 0;
	public static void main(String[] args) {
		SpringApplication.run(MultipleConsumerTest.class, 
				"--spring.datasource.url=jdbc:oracle:thin:@cdb1_pdb1_wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.wallet_location=C:/tmp/wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.tns_admin=C:/tmp/wallet",
				"--spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
				"--spring.datasource.type=oracle.ucp.jdbc.PoolDataSource",
				"--spring.datasource.oracleucp.connection-factory-class-name=oracle.jdbc.pool.OracleDataSource",
				"--spring.cloud.function.definition=cs1;cs2",
				"--spring.cloud.stream.default.destination=tst1",
				"--spring.cloud.stream.default.group=t1",
				"--spring.cloud.stream.default.consumer.partitioned=true",
				"--spring.cloud.stream.default.instanceCount=1",
				"--spring.cloud.stream.default.instanceIndex=0");//,
//				"--spring.cloud.stream.bindings.pd-out-0.producer.requiredGroups=t1",
//				"--spring.cloud.stream.bindings.pd-out-0.producer.poller.fixedDelay=100"); /**/
	}
	
	
	@Bean
	public Consumer<String> cs1() {
		return to -> System.out.println("Received for cs1: " + to);
	}
	
	@Bean
	public Consumer<String> cs2() {
		return to -> System.out.println("Received for cs2: " + to);
	}
	
	
//	@Bean
//	public Supplier<String> pd() {
//		return () -> {
//			System.out.println("Sending Message: ");
//			return "Message: " + (y++);
//		};
//	}
}