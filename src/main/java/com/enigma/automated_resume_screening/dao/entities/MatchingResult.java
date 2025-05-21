package com.enigma.automated_resume_screening.dao.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingResult {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne
    @JoinColumn(name = "job_offer_id")
    private JobOffer jobOffer;

    private double matchScore;

    @Column(columnDefinition = "TEXT")
    private String details; // JSON with match details
}
