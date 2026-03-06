package com.pig4cloud.pig.takeaway.user;

import com.pig4cloud.pig.common.feign.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

@EnablePigDoc("takeaway/customer")
@EnablePigFeignClients
@EnableDiscoveryClient
@AutoConfigurationPackage(basePackages = "com.pig4cloud.pig.takeaway")
@SpringBootApplication
public class PigTakeawayUserApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(PigTakeawayUserApplication.class)
			.beanNameGenerator(new FullyQualifiedAnnotationBeanNameGenerator())
			.run(args);
	}

}