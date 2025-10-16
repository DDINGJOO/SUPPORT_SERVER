package com.teambind.supportserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SupportServerApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(SupportServerApplication.class, args);
	}
	
}
