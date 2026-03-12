package com.luan.takeaway.takeaway.dish;

import com.luan.takeaway.common.security.annotation.EnablePigResourceServer;
import com.luan.takeaway.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnablePigDoc("takeaway/dish")
@EnablePigResourceServer
@EnableDiscoveryClient
@AutoConfigurationPackage(basePackages = "com.luan.takeaway.takeaway")
@SpringBootApplication(scanBasePackages = "com.luan.takeaway.takeaway")
public class PigTakeawayDishApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigTakeawayDishApplication.class, args);
	}

}
