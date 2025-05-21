package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {
    private final VectorStore vectorStore;

    public void embedResume(String candidateId, String resumeText) {
        // ✅ Limite de sécurité (car Ollama tolère rarement > 4096 tokens)
        final int MAX_LENGTH = 3000;

        // ✅ Tronquer si trop long
        if (resumeText.length() > MAX_LENGTH) {
            resumeText = resumeText.substring(0, MAX_LENGTH);
        }

        UUID docId = UUID.randomUUID();

        Document document = new Document(
                resumeText,
                docId.toString(),
                Map.of("source", "cv-parsing", "candidateId", candidateId)
        );

        // ✅ Ajouter dans PGVector
        vectorStore.write(List.of(document));

        System.out.println("✅ Embedding enregistré pour le candidat : " + candidateId);

    }
}
