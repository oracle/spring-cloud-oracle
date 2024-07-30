package producer.error;

import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.support.ErrorMessage;

@SpringBootApplication
public class ProducerErrorChannelTest {
	
	public static void main(String[] args) {
		SpringApplication.run(ProducerErrorChannelTest.class,
				"--spring.datasource.url=jdbc:oracle:thin:@cdb1_pdb1_wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.wallet_location=C:/tmp/wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.tns_admin=C:/tmp/wallet",
				"--spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
				"--spring.datasource.type=oracle.ucp.jdbc.PoolDataSource",
				"--spring.datasource.oracleucp.connection-factory-class-name=oracle.jdbc.pool.OracleDataSource",
				"--spring.cloud.function.definition=supplier_err",
				"--spring.cloud.stream.bindings.supplier_err-out-0.destination=atest2",
				"--spring.cloud.stream.bindings.supplier_err-out-0.group=g1",
				"--spring.cloud.stream.bindings.supplier_err-out-0.producer.errorChannelEnabled=true",
				"--spring.cloud.stream.bindings.supplier_err-out-0.producer.poller.fixedDelay=6000"
				);
	}
	
	@Bean
	public Supplier<String> supplier_err() {
		return () -> {
			System.out.println("Sending msg...");
			throw new RuntimeException("An error occurred..");
			// return "Test msg";
		};
	}
	
	/*@Bean
	public Consumer<String> cons_err() {
		return m -> System.out.println("Received: " + m);
	} */
	
	@ServiceActivator(inputChannel = "unknown.channel.name")
	public void handlePublishError(ErrorMessage message) {
	    System.out.println("Message Publish Failed for: " + message);
	    System.out.println("Original Message: " + message.getOriginalMessage());
	    System.out.println();
	}
	
}
