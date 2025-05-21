package com.enigma.automated_resume_screening.business.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CVParsingService {
    Map<String, Object> parseResume(MultipartFile file);
}
