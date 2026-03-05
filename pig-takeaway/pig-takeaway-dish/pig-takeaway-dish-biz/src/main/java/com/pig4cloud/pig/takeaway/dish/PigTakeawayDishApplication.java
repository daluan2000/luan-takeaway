package com.pig4cloud.pig.takeaway.dish;

import com.pig4cloud.pig.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnablePigDoc("takeaway-dish")
@EnableDiscoveryClient
@AutoConfigurationPackage(basePackages = "com.pig4cloud.pig.takeaway")
@SpringBootApplication
public class PigTakeawayDishApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigTakeawayDishApplication.class, args);
	}

}
