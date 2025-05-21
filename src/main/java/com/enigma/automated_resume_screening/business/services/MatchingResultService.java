package com.enigma.automated_resume_screening.business.services;

import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.entities.JobOffer;
import com.enigma.automated_resume_screening.dao.entities.MatchingResult;

import java.util.List;

public interface MatchingResultService {
    void saveMatchingResult(Candidate candidate, JobOffer jobOffer, int score, String commentaire);
    List<MatchingResult> getResultsByJobOfferId(Long jobOfferId);
    List<Candidate> getShortlistedCandidates(Long jobOfferId);
}
