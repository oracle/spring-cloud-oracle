package consumer.retry;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConsumerBatchTest {
	
	static int x = 0;
	public static void main(String[] args) {
		SpringApplication.run(ConsumerBatchTest.class,
				"--spring.datasource.url=jdbc:oracle:thin:@cdb1_pdb1_wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.wallet_location=C:/tmp/wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.tns_admin=C:/tmp/wallet",
				"--spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
				"--spring.datasource.type=oracle.ucp.jdbc.PoolDataSource",
				"--spring.datasource.oracleucp.connection-factory-class-name=oracle.jdbc.pool.OracleDataSource",
				"--spring.cloud.function.definition=supp;cons_batch",
				"--spring.cloud.stream.default.destination=atest1",
				"--spring.cloud.stream.default.group=g1",
				"--spring.cloud.stream.bindings.cons_batch-in-0.consumer.batchMode=true",
				"--spring.cloud.stream.txeventq.bindings.cons_batch-in-0.consumer.batchSize=3",
				"--spring.cloud.stream.txeventq.bindings.cons_batch-in-0.consumer.timeout=10000",
				"--spring.cloud.stream.bindings.supp-out-0.producer.requiredGroups=g1"
				);
	}
	
	@Bean
	public Supplier<Sample> supp() {
		return () -> new Sample(x++);
	}
	
	@Bean
	public Consumer<List<Sample>> cons_batch() {
		return m -> {
			System.out.println("Number of messages received: " + m.size());
			for(Sample s: m) {
				System.out.println("Message: " + s);
			}
			System.out.println();
		};
	}
}

class Sample {
	private int x;
	
	Sample() {
		this.x = 0;
	}
	
	Sample(int x) {
		this.x = x;
	}
	
	public int getX() {
		return this.x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public String toString() {
		return "Sample[x=" + this.x + "]";
	}
}
