package com.luan.takeaway.takeaway.order;

import com.luan.takeaway.common.feign.annotation.EnablePigFeignClients;
import com.luan.takeaway.common.security.annotation.EnablePigResourceServer;
import com.luan.takeaway.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnablePigDoc("takeaway/order")
@EnablePigResourceServer
@EnablePigFeignClients
@EnableDiscoveryClient
@AutoConfigurationPackage(basePackages = "com.luan.takeaway.takeaway")
@SpringBootApplication
public class PigTakeawayOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigTakeawayOrderApplication.class, args);
	}

}
