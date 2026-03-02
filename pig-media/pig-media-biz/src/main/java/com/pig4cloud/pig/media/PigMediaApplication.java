package com.pig4cloud.pig.media;

import com.pig4cloud.pig.common.feign.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import com.pig4cloud.pig.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 图床业务启动类
 *
 * @author pig
 */
@EnablePigDoc(value = "media")
@EnablePigFeignClients
@EnablePigResourceServer
@EnableDiscoveryClient
@SpringBootApplication
public class PigMediaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigMediaApplication.class, args);
	}

}
