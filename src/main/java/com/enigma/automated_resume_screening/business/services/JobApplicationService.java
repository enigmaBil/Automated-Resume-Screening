package com.enigma.automated_resume_screening.business.services;

import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.entities.JobApplication;
import com.enigma.automated_resume_screening.dao.entities.JobOffer;

public interface JobApplicationService {
    JobApplication createApplication(Candidate candidate, JobOffer jobOffer);
}
