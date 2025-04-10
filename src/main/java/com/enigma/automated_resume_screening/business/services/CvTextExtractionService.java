package com.enigma.automated_resume_screening.business.services;

import org.springframework.web.multipart.MultipartFile;

public interface CvTextExtractionService {
    /**
     * Extrait le texte brut d'un fichier CV
     * @param file
     * @return
     */
    String extractText(MultipartFile file);
}
