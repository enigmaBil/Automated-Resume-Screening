package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.MatchingResultService;
import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.entities.JobOffer;
import com.enigma.automated_resume_screening.dao.entities.MatchingResult;
import com.enigma.automated_resume_screening.dao.repositories.MatchingResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.enigma.automated_resume_screening.config.AppConstants.THRESHOLD_SCORE;

@Service
@RequiredArgsConstructor
public class MatchingResultServiceImpl implements MatchingResultService {
    private final MatchingResultRepository repository;

    @Override
    public void saveMatchingResult(Candidate candidate, JobOffer jobOffer, int score, String commentaire) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String detailsJson = mapper.writeValueAsString(Map.of(
                    "score", score,
                    "commentaire", commentaire
            ));

            MatchingResult result = MatchingResult.builder()
                    .candidate(candidate)
                    .jobOffer(jobOffer)
                    .matchScore(score)
                    .details(detailsJson)
                    .build();

            repository.save(result);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du MatchingResult", e);
        }
    }

    @Override
    public List<MatchingResult> getResultsByJobOfferId(Long jobOfferId) {
        return repository.findAll()
                .stream()
                .filter(m -> m.getJobOffer().getId().equals(jobOfferId))
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore())) // top-down
                .toList();
    }

    @Override
    public List<Candidate> getShortlistedCandidates(Long jobOfferId) {
        return repository
                .findByJobOfferIdAndMatchScoreGreaterThanEqual(jobOfferId, THRESHOLD_SCORE)
                .stream()
                .map(MatchingResult::getCandidate)
                .collect(Collectors.toList());
    }
}
