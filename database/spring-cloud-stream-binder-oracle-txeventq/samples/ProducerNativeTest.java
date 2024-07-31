package nativetests;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProducerNativeTest {
	int y = 0;
	
	public static void main(String[] args) {
		SpringApplication.run(ProducerNativeTest.class, 
				"--spring.datasource.url=jdbc:oracle:thin:@cdb1_pdb1_wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.wallet_location=C:/tmp/wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.tns_admin=C:/tmp/wallet",
				"--spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
				"--spring.datasource.type=oracle.ucp.jdbc.PoolDataSource",
				"--spring.datasource.oracleucp.connection-factory-class-name=oracle.jdbc.pool.OracleDataSource",
				"--spring.cloud.function.definition=produce2;consume2",
				"--spring.cloud.stream.default.destination=AADI_TEST",
				"--spring.cloud.stream.default.group=t1",
				"--spring.cloud.stream.bindings.produce2-out-0.producer.requiredGroups=t1",
				"--spring.cloud.stream.bindings.produce2-out-0.producer.use-native-encoding=true",
				"--spring.cloud.stream.txeventq.bindings.produce2-out-0.producer.serializer=nativetests.TestObjectSerializer",
				"--spring.cloud.stream.bindings.consume2-in-0.consumer.use-native-decoding=true",
				"--spring.cloud.stream.txeventq.bindings.consume2-in-0.consumer.deSerializer=nativetests.TestObjectDeserializer"
				);
	}
	
	@Bean
	public Supplier<TestObject> produce2() {
		System.out.println("Executing for first time.. Bean registration.");
		return () -> {
			System.out.println("Sending Message: ");
			return new TestObject(y++);
		};
	}
	
	@Bean
	public Consumer<TestObject> consume2() {
		return m -> System.out.println("Received: " + m);
	}
}