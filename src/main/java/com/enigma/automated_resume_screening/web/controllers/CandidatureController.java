    package com.enigma.automated_resume_screening.web.controllers;

    import com.enigma.automated_resume_screening.business.services.CandidateService;
    import com.enigma.automated_resume_screening.business.services.CvTextExtractionService;
    import com.enigma.automated_resume_screening.business.services.EmbeddingService;
    import com.enigma.automated_resume_screening.business.services.EmbeddingStorageService;
    import com.enigma.automated_resume_screening.business.services.JobApplicationService;
    import com.enigma.automated_resume_screening.business.services.ParsingService;
    import com.enigma.automated_resume_screening.business.services.RagService;
    import com.enigma.automated_resume_screening.config.FileStorageConfig;
    import com.enigma.automated_resume_screening.dao.entities.Candidate;
    import com.enigma.automated_resume_screening.dao.entities.JobOffer;
    import com.enigma.automated_resume_screening.dao.repositories.JobOfferRepository;
    import lombok.RequiredArgsConstructor;
    import org.apache.pdfbox.Loader;
    import org.apache.pdfbox.pdmodel.PDDocument;
    import org.springframework.core.io.FileSystemResource;
    import org.springframework.core.io.Resource;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.CrossOrigin;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.RequestPart;
    import org.springframework.web.bind.annotation.RestController;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.File;
    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.StandardCopyOption;
    import java.util.Arrays;

    @RestController
    @CrossOrigin("*")
    @RequestMapping("/api/candidates")
    @RequiredArgsConstructor
    public class CandidatureController {
        private final CandidateService candidateService;
        private final JobOfferRepository jobOfferRepository;
        private final JobApplicationService jobApplicationService;
        private final CvTextExtractionService cvTextExtractionService;
        private final ParsingService parsingService;
        private final EmbeddingService embeddingService;
        private final EmbeddingStorageService embeddingStorageService;
        private final RagService ragService;
        private final FileStorageConfig fileStorageConfig;

        @PostMapping(value = "/apply/{jobOfferId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<String> applyToJob(
                @PathVariable Long jobOfferId,
                @RequestParam("name") String name,
                @RequestParam("email") String email,
                @RequestParam("phone") String phone,
                @RequestParam("experienceYears") int experienceYears,
                @RequestParam("educationLevel") String educationLevel,
                @RequestParam("file") MultipartFile file) {

            try {
                JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                        .orElseThrow(() -> new RuntimeException("Offre d'emploi non trouvée"));

                if (file.isEmpty()) {
                    return ResponseEntity.badRequest().body("Aucun fichier CV fourni.");
                }

                Candidate candidate = candidateService.saveCandidate(name, email, phone, experienceYears, educationLevel, file);

                // Créer la candidature
                jobApplicationService.createApplication(candidate, jobOffer);

                Path uploadPath = fileStorageConfig.getUploadPath();
                File targetFile = new File(uploadPath.toFile(), file.getOriginalFilename());
                file.transferTo(targetFile);

                // Vérifie si le PDF est lisible
                try (var pdfDoc = Loader.loadPDF(targetFile)) {
                    Resource resource = new FileSystemResource(targetFile);
                    ragService.textEmbedding(new Resource[]{resource});
                } catch (IOException ioEx) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Le fichier fourni est corrompu ou non lisible : " + ioEx.getMessage());
                }

                String query = """
            Give me in json format, for each resume and for each candidat: candidat's personal information, education, professional experience, certifications, skills
            and project history.
            """;

                String response = ragService.extractDataFromLLM(query);
                System.out.println(response);

                // 🔁 ARCHIVE du fichier après traitement
                Path archivePath = uploadPath.resolve("archive");
                Files.createDirectories(archivePath);
                Path archivedFile = archivePath.resolve(file.getOriginalFilename());
                Files.move(targetFile.toPath(), archivedFile, StandardCopyOption.REPLACE_EXISTING);

                return ResponseEntity.ok("Candidature soumise avec succès !");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de la soumission de la candidature : " + e.getMessage());
            }
        }

    }
