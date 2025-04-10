package com.enigma.automated_resume_screening.dao.repositories;

import com.enigma.automated_resume_screening.dao.entities.MatchingResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingResultRepository extends JpaRepository<MatchingResult, Long> {
}