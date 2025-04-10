package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.ParsingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ParsingServiceImpl implements ParsingService {
    @Value("${spring.ai.ollama.generate-url}")
    private String ollamaApiUrl;

    private final VectorStore vectorStore;

    /**
     * Analyse le texte du CV avec Ollama.
     * @param cvText
     * @return
     */
    @Override
    public String analyzeCv(String cvText) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama3");
        requestBody.put("prompt", "Analyse ce CV et retourne sous forme de json, les compétences, l'expérience, les certifications et l'éducation ou la formation : " + cvText);

        System.out.println("Appel à Ollama avec l'URL : " + ollamaApiUrl);
        System.out.println("Corps de la requête : " + requestBody);

        try{
            String responseString = restTemplate.postForObject(ollamaApiUrl, requestBody, String.class);

            StringBuilder fullResponse = new StringBuilder();
            String[] lines = responseString.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    Map<String, Object> jsonResponse = new ObjectMapper().readValue(line, Map.class);
                    fullResponse.append((String) jsonResponse.get("response"));
                }
            }
            System.out.println("Réponse complète de Ollama : " + fullResponse);

            return fullResponse.toString();
        }catch (Exception e){
            throw new RuntimeException("Erreur lors de l'appel à Ollama : " + e.getMessage(), e);
        }
//        Map<String, Object> response = restTemplate.postForObject(ollamaApiUrl, requestBody, Map.class);
//        return  (String) response.get("response");
    }
}
