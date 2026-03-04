package com.pig4cloud.pig.takeaway.pay;

import com.pig4cloud.pig.common.feign.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnablePigDoc("takeaway-pay")
@EnablePigFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class PigTakeawayPayApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigTakeawayPayApplication.class, args);
	}

}
