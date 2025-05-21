package com.enigma.automated_resume_screening.web.controllers;

import com.enigma.automated_resume_screening.business.services.CVParsingService;
import com.enigma.automated_resume_screening.business.services.CandidateService;
import com.enigma.automated_resume_screening.business.services.JobApplicationService;
import com.enigma.automated_resume_screening.business.services.JobOfferService;
import com.enigma.automated_resume_screening.business.services.MatchingResultService;
import com.enigma.automated_resume_screening.business.services.MatchingService;
import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.entities.JobApplication;
import com.enigma.automated_resume_screening.dao.entities.JobOffer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidateService candidateService;
    private final JobOfferService jobOfferService;
    private final JobApplicationService jobApplicationService;
    private final CVParsingService cvParsingService;
    private final MatchingService matchingService;
    private final MatchingResultService matchingResultService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/apply/{jobOfferId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> applyToJob(
            @PathVariable Long jobOfferId,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("experienceYears") int experienceYears,
            @RequestParam("educationLevel") String educationLevel,
//            @RequestParam("skills") String skillsJson,
            @RequestParam("file") MultipartFile file) throws JsonProcessingException {
        try {
            // Étape 1 : Parser le CV
            Map<String, Object> parsed = cvParsingService.parseResume(file);
            Map<String, Object> llmResult = (Map<String, Object>) parsed.get("llm_result");
            System.out.println("Résultat LLM : "+llmResult);

            // 2. Récupération des compétences nettoyées
            Object skillsObj = parsed.get("normalized_skills");
            String skillsJson = objectMapper.writeValueAsString(skillsObj);

            // Étape 3 : Créer le candidat enrichi
            Candidate candidate = Candidate.builder()
                    .name(name)
                    .email(email)
                    .phone(phone)
                    .experienceYears(experienceYears)
                    .educationLevel(educationLevel)
                    .skills(skillsJson)
                    .build();

            Candidate savedCandidate = candidateService.saveCandidateWithCV(candidate, file);
            System.out.println(candidateService);

            //Créer la candidature
            JobOffer jobOffer = jobOfferService.getJobOffer(jobOfferId);
            JobApplication application = jobApplicationService.createApplication(savedCandidate, jobOffer);
            System.out.println(application);

            //Matching IA
            Map<String, Object> matchingResult = matchingService.matchCandidateToOffer(candidate, jobOffer);
            int score = (int) matchingResult.get("score");
            String commentaire = (String) matchingResult.get("commentaire");
            System.out.println(matchingResult);

            matchingResultService.saveMatchingResult(candidate, jobOffer, score, commentaire);

            return ResponseEntity.ok("Candidature enregistrée avec succès avec ID : " + application.getId());

        } catch (Exception e) {
            e.getMessage();
            return ResponseEntity.internalServerError().body("Candidature créée, mais erreur IA/parsing : " + e.getMessage());
        }
    }
}