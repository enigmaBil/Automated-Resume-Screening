package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.CandidateService;
import com.enigma.automated_resume_screening.config.FileStorageConfig;
import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.repositories.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {
    private final CandidateRepository candidateRepository;
    private final FileStorageConfig fileStorageConfig;

    /**
     * Enregistre un nouveau candidat avec son CV dans un dossier temporaire.
     */
    @Override
    public Candidate saveCandidate(String name, String email, String phone, int experienceYears, String educationLevel, MultipartFile file) throws IOException {

        // Enregistrer le fichier dans le répertoire temporaire
        Path uploadPath = fileStorageConfig.getUploadPath();
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename(); // Nom unique pour éviter les conflits
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // Créer le candidat avec le chemin du fichier
        Candidate candidate = Candidate.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .experienceYears(experienceYears)
                .educationLevel(educationLevel)
                .resumeFilePath(filePath.toString())
                .build();
        return candidateRepository.save(candidate);
    }
}
