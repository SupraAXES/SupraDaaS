package com.supra.daas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
@ComponentScan(basePackages = "com.supra.daas")
public class DaasApplication {

	public static void main(String[] args) {
		SpringApplication.run(DaasApplication.class, args);
	}

}
