package com.oracle.stream;

import org.springframework.boot.SpringApplication;

public class DemoTests {
	int y = 0;
	public static void main(String[] args) {
		SpringApplication.run(DemoTests.class, 
				"--spring.datasource.url=jdbc:oracle:thin:@cdb1_pdb1_wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.wallet_location=C:/tmp/wallet",
				"--spring.datasource.oracleucp.connection-properties.oracle.net.tns_admin=C:/tmp/wallet",
				"--spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
				"--spring.datasource.type=oracle.ucp.jdbc.PoolDataSource",
				"--spring.datasource.oracleucp.connection-factory-class-name=oracle.jdbc.pool.OracleDataSource",
				"--spring.cloud.function.definition=consume;produce1",
				"--spring.cloud.stream.default.destination=AADI_TEST",
				"--spring.cloud.stream.default.group=t1",
				"--spring.cloud.stream.bindings.produce1-out-0.producer.requiredGroups=t1"); /**/
	}
	
	
	@Bean
	public Consumer<TestObject> consume() {
		return to -> System.out.println("Received: " + to);
	}
	
	@Bean
	public Supplier<TestObject> produce1() {
		return () -> {
			System.out.println("Sending Message: ");
			return new TestObject(y++);
		};
	}
}

class TestObject {
	private int x;
	
	TestObject(int x) {
		this.x = x;
	}
	
	TestObject() {
		
	}
	
	public int getX() {
		return this.x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public String toString() {
		return "TestObject[x=" + this.x + "]";
	}
}