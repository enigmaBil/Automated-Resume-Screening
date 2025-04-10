package com.enigma.automated_resume_screening.business.services;

import com.enigma.automated_resume_screening.dao.entities.Candidate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CandidateService {
    Candidate saveCandidate(String name, String email, String phone, int experienceYears, String educationLevel, MultipartFile file) throws IOException;
}
