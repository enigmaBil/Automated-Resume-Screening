package com.enigma.automated_resume_screening.dao.repositories;

import com.enigma.automated_resume_screening.dao.entities.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
}