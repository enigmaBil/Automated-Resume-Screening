package com.enigma.automated_resume_screening.dao.repositories;

import com.enigma.automated_resume_screening.dao.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query(
            """
    select t from Token t inner join Rh r on t.rh.id = r.id
    where r.id = :rhId and (t.expired = false or t.revoked = false )
"""
    )
    List<Token> findAllValidTokenByRh(Long rhId);

    Optional<Token> findByToken(String token);
}
