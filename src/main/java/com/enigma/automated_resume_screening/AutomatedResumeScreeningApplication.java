package com.enigma.automated_resume_screening;

import com.enigma.automated_resume_screening.business.services.RagService;
import com.enigma.automated_resume_screening.config.FileStorageConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

@SpringBootApplication
public class AutomatedResumeScreeningApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutomatedResumeScreeningApplication.class, args);
	}

//	@Bean
//	CommandLineRunner commandLineRunner(RagService ragService, FileStorageConfig fileStorageConfig) {
//		return args -> {
//			Path uploadPath = fileStorageConfig.getUploadPath();
//			File[] files = uploadPath.toFile().listFiles((dir, name) ->
//					name.endsWith(".pdf") || name.endsWith(".docx") || name.endsWith(".doc")
//			);
//			Resource[] pdfResources = Arrays.stream(files).map(FileSystemResource::new).toArray(Resource[]::new);
//			ragService.textEmbedding(pdfResources);
//			String query = """
//			Give me in json format, for each resume and for each candidat: candidat's personal information, education, professional experience, certifications, skills
//			and project history.
//			""" ;
//
//			String response = ragService.extractDataFromLLM(query);
//			System.out.println(response);
//		};
//	}



}
