package com.hanu.AiEcommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiEcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiEcommerceApplication.class, args);
	}

}
