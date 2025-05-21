package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.CVParsingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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

            System.out.println("✅ Texte extrait du PDF :\n" + text);

      // 2. Prompt unique pour extraire directement les infos au format JSON
      String prompt =
          """
            Voici un CV brut. Donne uniquement les informations suivantes extraites du CV, en format JSON pur (sans aucun texte autour ni balise markdown):
            - nom complet
            - email
            - téléphone
            - compétences principales (sous forme: [Java, Spring Boot, etc...], enleve tout les slashs et rend chaque competence individuelle)
            - niveau d’éducation
            - années d’expérience
            Réponds strictement avec du JSON.
            CV :
            """
              + text;

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
            Object rawSkills = extractedData.get("compétences_principales");
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
