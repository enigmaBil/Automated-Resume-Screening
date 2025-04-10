package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.EmbeddingStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmbeddingStorageServiceImpl implements EmbeddingStorageService {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void storeEmbedding(String content, float[] embedding) {
        String sql = "INSERT INTO vector_store (id, content, metadata, embedding) VALUES (?, ?, ?::json, ?::vector)";
        jdbcTemplate.update(sql,
                UUID.randomUUID(),
                content,
                "{\"source\": \"CV\"}", // Exemple de métadonnées (JSON)
                embedding);
    }
}
