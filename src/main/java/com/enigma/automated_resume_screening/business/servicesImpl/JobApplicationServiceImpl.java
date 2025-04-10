package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.JobApplicationService;
import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.entities.JobApplication;
import com.enigma.automated_resume_screening.dao.entities.JobOffer;
import com.enigma.automated_resume_screening.dao.enums.ApplicationStatus;
import com.enigma.automated_resume_screening.dao.repositories.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    @Override
    public JobApplication createApplication(Candidate candidate, JobOffer jobOffer) {
        JobApplication jobApplication = JobApplication.builder()
                .candidate(candidate)
                .jobOffer(jobOffer)
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();
        return jobApplicationRepository.save(jobApplication);
    }
}
