package com.pig4cloud.pig.takeaway.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PigTakeawayOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigTakeawayOrderApplication.class, args);
	}

}
