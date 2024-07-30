package producer.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

@SpringBootApplication
public class BatchProducerTest {
	public static void main(String[] args) {
		SpringApplication.run(BatchProducerTest.class,
				"--spring.datasource.url=jdbc:oracle:thin:@cdb1_pdb1_wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.wallet_location=C:/tmp/wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.tns_admin=C:/tmp/wallet",
				"--spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
				"--spring.datasource.type=oracle.ucp.jdbc.PoolDataSource",
				"--spring.datasource.oracleucp.connection-factory-class-name=oracle.jdbc.pool.OracleDataSource",
				
				"--spring.cloud.function.definition=supply_batch;consume_batch;supply_simple;consume_batch2",
				"--spring.cloud.stream.default.destination=atest6",
				"--spring.cloud.stream.default.group=g1",
				"--spring.cloud.stream.bindings.supply_batch-out-0.producer.poller.fixedDelay=60000",
				"--spring.cloud.stream.bindings.supply_simple-out-0.producer.requiredGroups=g1"
				);
	}
	
	@Bean
	public Function<String, List<Message<String>>> supply_batch() {
		return v -> {
			System.out.println("Sending 3 msgs...");
			List<Message<String>> msgs = new ArrayList<Message<String>>();
			msgs.add(MessageBuilder.withPayload("Hey_next").build());
			msgs.add(MessageBuilder.withPayload("Hey_next").build());
			msgs.add(MessageBuilder.withPayload("Hey_next").build());
			return msgs;
		};
	}
	
	@Bean
	public Consumer<String> consume_batch() {
		return m -> System.out.println("Received: " + m);
	}
	
	@Bean
	public Consumer<Message<String>> consume_batch2() {
		return m -> System.out.println("Received for message payload: " + m);
	}
	
	// simple producer to supply string messages
	@Bean
	public Supplier<String> supply_simple() {
		return () -> "Hey";
	}
}
