package com.pig4cloud.pig.takeaway.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PigTakeawayMerchantApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigTakeawayMerchantApplication.class, args);
	}

}
