package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.EmbeddingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmbeddingServiceImpl implements EmbeddingService {
    @Value("${spring.ai.ollama.embed-url}")
    private String ollamaApiUrl;

    /**
     * Génère un embedding pour un texte donné.
     * @param text
     * @return
     */
    @Override
    public float[] generateEmbedding(String text) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama3");
        requestBody.put("prompt", text);

        Map<String, Object> response = restTemplate.postForObject(ollamaApiUrl, requestBody, Map.class);
        return (float[]) response.get("embedding");
    }
}
