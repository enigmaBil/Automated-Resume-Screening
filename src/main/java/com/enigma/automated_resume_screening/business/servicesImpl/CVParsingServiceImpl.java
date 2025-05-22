package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.CVParsingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CVParsingServiceImpl implements CVParsingService {
    private static final Logger log = LoggerFactory.getLogger(CVParsingServiceImpl.class);
    private final OllamaService ollamaService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> parseResume(MultipartFile file) {
        try {
            // 1. Lire le fichier PDF
            PDDocument document = PDDocument.load(file.getInputStream());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            document.close();

            if (text == null || text.trim().isEmpty()) {
                throw new RuntimeException("Le CV est vide ou non lisible.");
            }

            log.info("Texte extrait : {}", text);

      // 2. Prompt unique pour extraire directement les infos au format JSON
            String prompt =
                    """
                    Voici un CV brut. Extrait uniquement les informations suivantes au format JSON pur, sans aucun texte autour ni balise markdown :
                
                    {
                      "name": "",
                      "email": "",
                      "phone": "",
                      "mainSkills": ["Java", "Spring Boot", ...],
                      "educationLevel": "",
                      "yearsOfExperience": 0
                    }
                
                    Important :
                    - Donne uniquement ce JSON, sans explication.
                    - Sépare bien chaque compétence individuellement (pas de slashs, pas de groupements).
                    - "yearsOfExperience" doit être un entier (0 si non précisé).
                    
                    CV :
                    """ + text;


            // Appel au LLM
            String llamaResponse = ollamaService.callLlama3(prompt);

            Map<String, Object> outer = objectMapper.readValue(llamaResponse, new TypeReference<>() {});
            Map<String, Object> message = (Map<String, Object>) outer.get("message");
            String content = (String) message.get("content");

            int jsonStart = content.indexOf("{");
            int jsonEnd = content.lastIndexOf("}") + 1;
            String jsonBlock = content.substring(jsonStart, jsonEnd).trim();

            //  parsing final
            Map<String, Object> extractedData = objectMapper.readValue(jsonBlock, new TypeReference<>() {});
            // Nettoyage des compétences
            Object rawSkills = extractedData.get("mainSkills");
            List<String> cleanedSkills = normalizeSkills(rawSkills);

            Map<String, Object> output = new HashMap<>();
            output.put("llm_result", extractedData);
            output.put("normalized_skills", cleanedSkills);
            return output;

        } catch (Exception e) {
            throw new RuntimeException("Erreur de parsing de CV", e);
        }
    }

    private List<String> normalizeSkills(Object rawSkills) {
        if (rawSkills instanceof List<?>) {
            return ((List<?>) rawSkills).stream()
                    .flatMap(skill -> Arrays.stream(skill.toString().split("[/,-]")))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .collect(Collectors.toList());
        }
        return List.of();
    }

}
