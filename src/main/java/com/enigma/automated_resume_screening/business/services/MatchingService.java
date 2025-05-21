package com.enigma.automated_resume_screening.business.services;

import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.entities.JobOffer;

import java.util.Map;

public interface MatchingService {
    Map<String, Object> matchCandidateToOffer(Candidate candidate, JobOffer offer);
}
