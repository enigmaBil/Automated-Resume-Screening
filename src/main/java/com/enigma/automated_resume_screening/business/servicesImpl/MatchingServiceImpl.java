package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.MatchingService;
import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.entities.JobOffer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {
    private final OllamaService ollamaService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> matchCandidateToOffer(Candidate candidate, JobOffer jobOffer) {
        try {
      String prompt =
          """
            Tu es un système de recrutement. Compare les informations suivantes et retourne un JSON au format :
            {
              "score": 80,
              "commentaire": "Le candidat a de solides compétences en Java et Spring Boot, mais manque d'expérience en Docker."
            }

            CANDIDAT :
            - Nom : %s
            - Email : %s
            - Téléphone : %s
            - Expérience : %d années
            - Niveau d'éducation : %s
            - Competences : %s

            OFFRE D’EMPLOI :
            - Titre : %s
            - Compétences requises : %s
            - Expérience requise : %d années
            - Niveau d'éducation requis : %s
            """
              .formatted(
                  candidate.getName(),
                  candidate.getEmail(),
                  candidate.getPhone(),
                  candidate.getExperienceYears(),
                  candidate.getEducationLevel(),
                  candidate.getSkills(),
                  jobOffer.getTitle(),
                  jobOffer.getRequiredSkills(),
                  jobOffer.getExperienceYears(),
                  jobOffer.getEducationLevel());

            String rawResponse = ollamaService.callLlama3(prompt);
            System.out.println("Réponse LLaMA : \n" + rawResponse);

            if (rawResponse == null || rawResponse.trim().isEmpty()) {
                throw new RuntimeException("Réponse vide de LLaMA");
            }
            Map<String, Object> outerJson = objectMapper.readValue(rawResponse, new TypeReference<>() {});
            Map<String, Object> message = (Map<String, Object>) outerJson.get("message");
            String content = (String) message.get("content");

            System.out.println("Contenu texte du LLM : \n" + content);

            int score = 0;
            String commentaire = "Non défini";

            if (content.toLowerCase().contains("score")) {
                try {
                    String scoreLine = content.substring(content.toLowerCase().indexOf("score"))
                            .split("\n")[0]
                            .replaceAll("[^0-9]", "");
                    score = Integer.parseInt(scoreLine);
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }

            if (content.toLowerCase().contains("commentaire")) {
                commentaire = content.substring(content.toLowerCase().indexOf("commentaire")).split(":", 2)[1].trim();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("score", score);
            result.put("commentaire", commentaire);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Erreur de matching avec LLaMA", e);
        }
    }

}
