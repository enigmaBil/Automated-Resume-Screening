package com.enigma.automated_resume_screening.business.servicesImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OllamaService {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";

    public String callLlama3(String promptText) {
        // Construire le corps de la requête
        Map<String, Object> body = Map.of(
                "model", "llama3",
                "messages", List.of(
                        Map.of("role", "system", "content", "Tu es un assistant de recrutement."),
                        Map.of("role", "user", "content", promptText)
                ),
                "stream", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // 2. Appel
        ResponseEntity<String> response = restTemplate.exchange(
                OLLAMA_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        return response.getBody();

    }
}
