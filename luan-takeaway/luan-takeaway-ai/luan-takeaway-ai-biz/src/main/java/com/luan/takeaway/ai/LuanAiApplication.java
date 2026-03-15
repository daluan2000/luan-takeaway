package com.luan.takeaway.ai;

import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.common.feign.annotation.EnablePigFeignClients;
import com.luan.takeaway.common.security.annotation.EnablePigResourceServer;
import com.luan.takeaway.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * AI 服务启动入口。
 * <p>
 * 该模块独立部署为一个微服务，对外提供 AI 推荐能力，
 * 并通过 Feign 与菜品/商家/订单等模块协同完成推荐闭环。
 */
@EnablePigDoc("takeaway/ai")
@EnablePigResourceServer
@EnablePigFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties(AiAssistantProperties.class)
public class LuanAiApplication {

	/**
	 * Spring Boot 标准启动方法。
	 */
	public static void main(String[] args) {
		SpringApplication.run(LuanAiApplication.class, args);
	}

}