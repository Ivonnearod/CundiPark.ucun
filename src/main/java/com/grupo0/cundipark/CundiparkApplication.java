package com.grupo0.cundipark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CundiparkApplication {

	public static void main(String[] args) {
		SpringApplication.run(CundiparkApplication.class, args);
	}

	@Bean
	public CommandLineRunner iniciarConsola() {
		return args -> {
			System.out.println("\n----------------------------------------------------------");
			System.out.println("✅ APLICACIÓN CUNDIPARK INICIADA CORRECTAMENTE");
			System.out.println("🌐 Accede en: http://localhost:8080");
			System.out.println("----------------------------------------------------------\n");
		};
	}

}
