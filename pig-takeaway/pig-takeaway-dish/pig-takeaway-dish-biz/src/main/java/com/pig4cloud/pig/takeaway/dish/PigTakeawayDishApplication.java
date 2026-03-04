package com.pig4cloud.pig.takeaway.dish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PigTakeawayDishApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigTakeawayDishApplication.class, args);
	}

}
