package com.pig4cloud.pig.takeaway.pay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PigTakeawayPayApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigTakeawayPayApplication.class, args);
	}

}
