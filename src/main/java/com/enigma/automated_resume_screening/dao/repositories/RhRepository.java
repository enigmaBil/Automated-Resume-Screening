package com.enigma.automated_resume_screening.dao.repositories;

import com.enigma.automated_resume_screening.dao.entities.Rh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RhRepository extends JpaRepository<Rh, Long> {
    Optional<Rh> findByEmail(String email);
}
