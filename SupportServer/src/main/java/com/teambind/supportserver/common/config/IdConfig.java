package com.teambind.supportserver.common.config;

import com.teambind.supportserver.common.utils.IdGenerator;
import com.teambind.supportserver.common.utils.Snowflake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdConfig {
	
	@Bean
	public IdGenerator idGenerator() {
		return new Snowflake();
	}
}
