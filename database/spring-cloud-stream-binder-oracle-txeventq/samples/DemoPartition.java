package com.oracle.stream.partitions;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
public class DemoPartition {
	public static void main(String[] args) {
		SpringApplication.run(DemoPartition.class,
				// connection related properties
				"--spring.datasource.url=jdbc:oracle:thin:@cdb1_pdb1_wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.wallet_location=C:/tmp/wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.tns_admin=C:/tmp/wallet",
				"--spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
				"--spring.datasource.type=oracle.ucp.jdbc.PoolDataSource",
				"--spring.datasource.oracleucp.connection-factory-class-name=oracle.jdbc.pool.OracleDataSource",
				// required beans and common properties to all beans
				"--spring.cloud.function.definition=produce;consume1;consume2",
				"--spring.cloud.stream.default.destination=atest4",
				"--spring.cloud.stream.default.group=g1",
				// properties specific to producer for partitions
				"--spring.cloud.stream.bindings.produce-out-0.producer.requiredGroups=g1",
				"--spring.cloud.stream.bindings.produce-out-0.producer.partitionKeyExpression=headers['partitionKey']",
				"--spring.cloud.stream.bindings.produce-out-0.producer.partitionCount=2",
				// "--spring.cloud.stream.bindings.produce-out-0.producer.autoStartup=false",
				// properties specific to consumer 1
				"--spring.cloud.stream.bindings.consume1-in-0.consumer.instanceCount=2",
				"--spring.cloud.stream.bindings.consume1-in-0.consumer.instanceIndex=0",
				// properties specific to consumer 2
				"--spring.cloud.stream.bindings.consume2-in-0.consumer.instanceCount=2",
				"--spring.cloud.stream.bindings.consume2-in-0.consumer.instanceIndex=1"
				);
	}
	
	
	static int x = 1;
	static int y = 0;
	
	@Bean
	public Supplier<Message<?>> produce() {
  		return () -> {
  			System.out.println("Sending message to partition: " + x);
			x = (x+1)%2;
    		String value ="payload-" + x;
			value += "id="+(y++);
			return MessageBuilder.withPayload(value)
           			.setHeader("partitionKey", x)
           			.build();
	  	};
	}
	
	@Bean
	public Consumer<String> consume1() {
		return m -> System.out.println("Received for consume1: " + m);
	}

	@Bean
	public Consumer<String> consume2() {
		return m -> System.out.println("Received for consume2: " + m);
	}
}
