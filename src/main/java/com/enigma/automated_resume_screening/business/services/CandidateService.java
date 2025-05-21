package com.enigma.automated_resume_screening.business.services;

import com.enigma.automated_resume_screening.dao.entities.Candidate;
import org.springframework.web.multipart.MultipartFile;

public interface CandidateService {
    Candidate saveCandidateWithCV(Candidate candidate, MultipartFile file);
}
