package producer.error;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.ErrorMessage;

@SpringBootApplication
public class ErrorHandlerTest {
	
	public static void main(String[] args) {
		SpringApplication.run(ErrorHandlerTest.class, 
				"--spring.datasource.url=jdbc:oracle:thin:@cdb1_pdb1_wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.wallet_location=C:/tmp/wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.tns_admin=C:/tmp/wallet",
				"--spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
				"--spring.datasource.type=oracle.ucp.jdbc.PoolDataSource",
				"--spring.datasource.oracleucp.connection-factory-class-name=oracle.jdbc.pool.OracleDataSource",
				"--spring.cloud.function.definition=err_supply;err_consume",
				"--spring.cloud.stream.default.destination=errtopic",
				"--spring.cloud.stream.default.group=t1",
				"--spring.cloud.stream.default.error-handler-definition=myerrHandler",
				"--spring.cloud.stream.bindings.err_supply-out-0.producer.requiredGroups=t1");
	}
	
	@Bean
	public Supplier<String> err_supply() {
		return () -> {
			System.out.println("Sending exception...");
			return "Hey";
		};
	}
	
	@Bean
	public Consumer<String> err_consume() {
		return m -> {
			System.out.println("Processing received message...");
			throw new RuntimeException("Custom error...");
		};
	}
	
	@Bean
	public Consumer<ErrorMessage> myerrHandler() {
		return m -> System.out.println("Received: " + m);
	}
}
