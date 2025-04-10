package com.enigma.automated_resume_screening.dao.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {
    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private Long id;

    private String name;

    private String email;

    private String phone;

//    @Column(columnDefinition = "jsonb")
//    private String skills; // JSON list of extracted skills

    private int experienceYears;

    private String educationLevel;
    @Column(name = "resume_file_path")
    private String resumeFilePath; // Chemin vers le fichier CV dans le répertoire temporaire
}
