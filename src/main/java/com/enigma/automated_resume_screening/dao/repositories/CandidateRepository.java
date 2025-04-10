package com.enigma.automated_resume_screening.dao.repositories;

import com.enigma.automated_resume_screening.dao.entities.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
}