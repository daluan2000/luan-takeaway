package com.luan.takeaway.ai;

import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.common.feign.annotation.EnablePigFeignClients;
import com.luan.takeaway.common.security.annotation.EnablePigResourceServer;
import com.luan.takeaway.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnablePigDoc("takeaway/ai")
@EnablePigResourceServer
@EnablePigFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties(AiAssistantProperties.class)
public class LuanAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LuanAiApplication.class, args);
	}

}