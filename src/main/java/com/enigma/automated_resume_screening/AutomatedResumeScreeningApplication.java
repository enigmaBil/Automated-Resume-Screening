package com.enigma.automated_resume_screening;

import com.enigma.automated_resume_screening.dao.repositories.RhRepository;
import com.enigma.automated_resume_screening.web.controllers.auth.AuthenticateService;
import com.enigma.automated_resume_screening.web.controllers.auth.RegisterRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AutomatedResumeScreeningApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutomatedResumeScreeningApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			AuthenticateService service,
			RhRepository rhRepository
	) {
		return args -> {
			String adminEmail = "admin@mail.com";

			// Vérifie si l'utilisateur existe déjà
			if (rhRepository.findByEmail(adminEmail).isEmpty()) {
				var admin = RegisterRequest.builder()
						.name("Admin")
						.email(adminEmail)
						.password("password")
						.build();

				System.out.println("Admin access Token: " + service.register(admin).getAccessToken());
			} else {
				System.out.println("Admin already exists. Skipping creation.");
			}
		};
	}



}
