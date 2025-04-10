package com.enigma.automated_resume_screening.business.services;

import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.entities.JobOffer;
import com.enigma.automated_resume_screening.dao.entities.MatchingResult;
import com.enigma.automated_resume_screening.dao.repositories.CandidateRepository;
import com.enigma.automated_resume_screening.dao.repositories.JobOfferRepository;
import com.enigma.automated_resume_screening.dao.repositories.MatchingResultRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MatchingService {
    private final JdbcTemplate jdbcTemplate;
    private final JobOfferRepository jobOfferRepository;
    private final CandidateRepository candidateRepository;
    private final MatchingResultRepository matchingResultRepository;

    private static final double MATCH_THRESHOLD = 0.7;

    public List<Candidate> findMatchingCandidates(Long jobOfferId) throws JsonProcessingException {

        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new RuntimeException("Job offer not found"));

        String sql = "SELECT candidate_id, 1 - (embedding <=> (SELECT embedding FROM vector_store WHERE job_offer_id = ?)) AS score " +
                "FROM vector_store ORDER BY score DESC LIMIT 10";

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, jobOfferId);

        List<Candidate> matchedCandidates = new ArrayList<>();
        for (Map<String, Object> row : results) {
            Long candidateId = (Long) row.get("candidate_id");
            double embeddingScore = (double) row.get("score");
            Candidate candidate = candidateRepository.findById(candidateId).orElse(null);

            if (candidate != null) {
                double finalScore = calculateFinalScore(candidate, jobOffer, embeddingScore);
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> detailsMap = Map.of("matchDetails", "Score based on embedding similarity and weighted criteria");

                String jsonDetails = objectMapper.writeValueAsString(detailsMap);
                if (finalScore >= MATCH_THRESHOLD) {
                    MatchingResult matchingResult = MatchingResult.builder()
                            .candidate(candidate)
                            .jobOffer(jobOffer)
                            .matchScore(finalScore)
                            .details(jsonDetails)
                            .build();
                    matchingResultRepository.save(matchingResult);
                    matchedCandidates.add(candidate);
                }
            }
        }
        return matchedCandidates;
    }
    private double calculateFinalScore(Candidate candidate, JobOffer jobOffer, double embeddingScore) {
        // Parse job offer weighting JSON
        Map<String, Double> weighting = parseWeighting(jobOffer.getWeighting());

        //double skillScore = calculateSkillScore(candidate.getSkills(), jobOffer.getRequiredSkills()) * weighting.getOrDefault("skills", 0.5);
        double experienceScore = calculateExperienceScore(candidate.getExperienceYears(), jobOffer.getExperienceYears()) * weighting.getOrDefault("experience", 0.3);
        double educationScore = calculateEducationScore(candidate.getEducationLevel(), jobOffer.getEducationLevel()) * weighting.getOrDefault("education", 0.2);
        return embeddingScore * 0.5 + experienceScore + educationScore;
        //return embeddingScore * 0.5 + skillScore + experienceScore + educationScore;
    }

    private double calculateSkillScore(String candidateSkills, String requiredSkills) {
        // Implement logic to compare JSON skill lists
        return 1.0; // Placeholder
    }

    private double calculateExperienceScore(int candidateExp, int requiredExp) {
        return Math.min(1.0, (double) candidateExp / requiredExp);
    }

    private double calculateEducationScore(String candidateEdu, String requiredEdu) {
        return candidateEdu.equalsIgnoreCase(requiredEdu) ? 1.0 : 0.0;
    }

    private Map<String, Double> parseWeighting(String weightingJson) {
        // Implement JSON parsing logic
        return Map.of("skills", 0.5, "experience", 0.3, "education", 0.2); // Placeholder
    }
}
