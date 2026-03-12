package com.luan.takeaway.takeaway.user;

import com.luan.takeaway.common.feign.annotation.EnablePigFeignClients;
import com.luan.takeaway.common.security.annotation.EnablePigResourceServer;
import com.luan.takeaway.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

@EnablePigDoc("takeaway/customer")
@EnablePigResourceServer
@EnablePigFeignClients
@EnableDiscoveryClient
@AutoConfigurationPackage(basePackages = "com.luan.takeaway.takeaway")
@SpringBootApplication(scanBasePackages = "com.luan.takeaway.takeaway")
public class PigTakeawayUserApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(PigTakeawayUserApplication.class)
			.beanNameGenerator(new FullyQualifiedAnnotationBeanNameGenerator())
			.run(args);
	}

}