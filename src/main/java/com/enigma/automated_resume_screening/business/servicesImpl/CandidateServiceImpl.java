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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {
    private final CandidateRepository candidateRepository;
    private final FileStorageConfig fileStorageConfig;

    public Candidate saveCandidateWithCV(Candidate candidate, MultipartFile file) {
        try {

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destinationPath = fileStorageConfig.getUploadPath().resolve(filename);

            // Sauvegarde le fichier sur le disque
            Files.copy(file.getInputStream(), destinationPath);

            // Met à jour le chemin du fichier dans le candidat
            candidate.setResumeFilePath(destinationPath.toString());

            return candidateRepository.save(candidate);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier CV", e);
        }
    }
}
