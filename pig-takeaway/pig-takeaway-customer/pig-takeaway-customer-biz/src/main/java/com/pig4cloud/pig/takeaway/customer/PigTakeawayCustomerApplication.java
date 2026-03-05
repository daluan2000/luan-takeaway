package com.pig4cloud.pig.takeaway.customer;

import com.pig4cloud.pig.common.feign.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnablePigDoc("takeaway-customer")
@EnablePigFeignClients
@EnableDiscoveryClient
@AutoConfigurationPackage(basePackages = "com.pig4cloud.pig.takeaway")
@SpringBootApplication
public class PigTakeawayCustomerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigTakeawayCustomerApplication.class, args);
	}

}
