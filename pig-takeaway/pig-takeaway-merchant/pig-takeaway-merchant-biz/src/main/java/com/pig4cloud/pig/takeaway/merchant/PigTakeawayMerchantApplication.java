package com.pig4cloud.pig.takeaway.merchant;

import com.pig4cloud.pig.common.feign.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnablePigDoc("takeaway-merchant")
@EnablePigFeignClients
@EnableDiscoveryClient
@AutoConfigurationPackage(basePackages = "com.pig4cloud.pig.takeaway")
@SpringBootApplication
public class PigTakeawayMerchantApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigTakeawayMerchantApplication.class, args);
	}

}
