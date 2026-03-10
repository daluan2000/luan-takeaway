package com.pig4cloud.pig.takeaway.order;

import com.pig4cloud.pig.common.feign.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import com.pig4cloud.pig.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnablePigDoc("takeaway/order")
@EnablePigResourceServer
@EnablePigFeignClients
@EnableDiscoveryClient
@AutoConfigurationPackage(basePackages = "com.pig4cloud.pig.takeaway")
@SpringBootApplication
public class PigTakeawayOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigTakeawayOrderApplication.class, args);
	}

}
