package com.pig4cloud.pig.takeaway.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PigTakeawayDeliveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigTakeawayDeliveryApplication.class, args);
	}

}
