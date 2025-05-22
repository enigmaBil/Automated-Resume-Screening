package com.enigma.automated_resume_screening.dao.entities;

import com.enigma.automated_resume_screening.config.StringListToJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    private int experienceYears;

    private String educationLevel;
    @Column(name = "resume_file_path")
    private String resumeFilePath;
}
