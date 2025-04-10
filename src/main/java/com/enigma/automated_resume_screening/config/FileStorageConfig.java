package com.enigma.automated_resume_screening.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileStorageConfig {
    public static final String UPLOAD_DIR =  System.getProperty("user.dir") + "/src/main/resources/uploads";

    @PostConstruct
    public void initialize() {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!uploadPath.toFile().exists()) {
            uploadPath.toFile().mkdirs();
            System.out.println("Répertoire uploads créé : " + uploadPath);
        } else {
            System.out.println("Répertoire uploads déjà existant : " + uploadPath);
        }
    }

    public Path getUploadPath() {
        return Paths.get(UPLOAD_DIR);
    }
}
