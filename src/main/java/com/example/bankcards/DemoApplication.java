package com.example.bankcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
        info = @Info(
                title = "Bank Cards API",
                version = "1.0.0",
                description = "REST API для управления банковскими картами: создание, статусы, поиск, операции"
        )
)
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		System.out.println("=== APPLICATION LAUNCHED ===");
	}
}
