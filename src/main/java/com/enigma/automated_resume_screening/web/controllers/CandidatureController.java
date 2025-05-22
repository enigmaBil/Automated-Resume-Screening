package com.enigma.automated_resume_screening.web.controllers;

import com.enigma.automated_resume_screening.business.services.CVParsingService;
import com.enigma.automated_resume_screening.business.services.CandidateService;
import com.enigma.automated_resume_screening.business.services.JobApplicationService;
import com.enigma.automated_resume_screening.business.services.JobOfferService;
import com.enigma.automated_resume_screening.business.services.MatchingResultService;
import com.enigma.automated_resume_screening.business.services.MatchingService;
import com.enigma.automated_resume_screening.business.services.NotificationService;
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

import static com.enigma.automated_resume_screening.config.AppConstants.THRESHOLD_SCORE;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidatureController {
    private final NotificationService notificationService;
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
            @RequestParam("file") MultipartFile file) throws JsonProcessingException {
        try {
            // Étape 1 : Parser le CV
            Map<String, Object> parsed = cvParsingService.parseResume(file);
            Map<String, Object> llmResult = (Map<String, Object>) parsed.get("llm_result");
            System.out.println("Résultat LLM : "+llmResult);
            Object skillsObj = parsed.get("normalized_skills");
            String skillsJson = objectMapper.writeValueAsString(skillsObj);

            Object experienceValue = llmResult.get("yearsOfExperience");
            int experienceYears = 0;
            if (experienceValue != null && !experienceValue.toString().equalsIgnoreCase("null")) {
                try {
                    experienceYears = Integer.parseInt(experienceValue.toString());
                } catch (NumberFormatException e) {
                    experienceYears = 0;
                }
            }

            // Étape 3 : Créer le candidat enrichi
            Candidate candidate = Candidate.builder()
                    .name((String) llmResult.get("name"))
                    .email((String) llmResult.get("email"))
                    .phone((String) llmResult.get("phone"))
                    .experienceYears(experienceYears)
                    .educationLevel((String) llmResult.get("educationLevel"))
                    .skills(skillsJson)
                    .build();

            Candidate savedCandidate = candidateService.saveCandidateWithCV(candidate, file);

            //Créer la candidature
            JobOffer jobOffer = jobOfferService.getJobOffer(jobOfferId);
            JobApplication application = jobApplicationService.createApplication(savedCandidate, jobOffer);
            System.out.println(application);

            //Matching IA
            Map<String, Object> matchingResult = matchingService.matchCandidateToOffer(candidate, jobOffer);
            int score = (int) matchingResult.get("score");
            String commentaire = String.valueOf(matchingResult.get("commentaire"));;
            System.out.println(matchingResult);

            matchingResultService.saveMatchingResult(candidate, jobOffer, score, commentaire);
            if (score >= THRESHOLD_SCORE) {
                String hrEmail = "emmanueldigital9@gmail.com";
                notificationService.sendShortlistNotification(hrEmail, savedCandidate, jobOffer, score);
            }
            return ResponseEntity.ok("Candidature créée avec succès pour : " + candidate.getName());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Candidature créée, mais erreur IA/parsing : " + e.getMessage());
        }
    }
}