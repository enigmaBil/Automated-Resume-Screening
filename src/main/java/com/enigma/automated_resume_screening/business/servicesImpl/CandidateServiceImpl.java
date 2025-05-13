package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.CandidateService;
import com.enigma.automated_resume_screening.business.services.RagService;
import com.enigma.automated_resume_screening.config.FileStorageConfig;
import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.repositories.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {
    private final CandidateRepository candidateRepository;
    private final FileStorageConfig fileStorageConfig;
    private final RagService ragService;

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

    public File storeFile(MultipartFile file) throws IOException {
        Path uploadPath = fileStorageConfig.getUploadPath();
        File targetFile = new File(uploadPath.toFile(), file.getOriginalFilename());
        file.transferTo(targetFile);
        return targetFile;
    }

    public void processPdfFile(File targetFile) throws IOException {
        // Vérifie si le PDF est lisible
        try (var pdfDoc = Loader.loadPDF(targetFile)) {
            ragService.textEmbedding(new Resource[]{new FileSystemResource(targetFile)});
        } catch (IOException ioEx) {
            throw new RuntimeException("Le fichier fourni est corrompu ou non lisible : " + ioEx.getMessage());
        }
    }

    public void archiveFile(File targetFile) throws IOException {
        // 🔁 ARCHIVE du fichier après traitement
        Path uploadPath = fileStorageConfig.getUploadPath();
        Path archivePath = uploadPath.resolve("archive");
        Files.createDirectories(archivePath);
        Path archivedFile = archivePath.resolve(targetFile.getName());
        Files.move(targetFile.toPath(), archivedFile, StandardCopyOption.REPLACE_EXISTING);
    }
}
