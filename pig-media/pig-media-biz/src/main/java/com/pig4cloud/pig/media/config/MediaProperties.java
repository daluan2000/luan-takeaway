package com.pig4cloud.pig.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 图床业务配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "media")
public class MediaProperties {

	private Share share = new Share();

	private Upload upload = new Upload();

	@Data
	public static class Share {

		private int defaultExpireHours = 24;

		private int defaultMaxViewCount = 1000;

	}

	@Data
	public static class Upload {

		private long maxSize = 10 * 1024 * 1024;

		private String allowedTypes = "image/jpeg,image/png,image/webp,image/gif,image/jpg";

	}

}
