package com.teambind.supportserver.report.config;

import com.teambind.supportserver.report.utils.IdGenerator;
import com.teambind.supportserver.report.utils.Snowflake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdConfig {
	
	@Bean
	public IdGenerator idGenerator() {
		return new Snowflake();
	}
}
