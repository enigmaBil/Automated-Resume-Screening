package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {
    @Value("${spring.ai.ollama.embed-url}")
    private String ollamaApiUrl;

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

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

    public void textEmbedding(String pdfResource) {
        // Supprime les données existantes dans le vector store
        jdbcTemplate.update("delete from vector_store");

        // Configuration du lecteur de PDF
        PdfDocumentReaderConfig pdfDocumentReaderConfig = PdfDocumentReaderConfig.defaultConfig();
        PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource, pdfDocumentReaderConfig);

        // Découpage du texte en tokens
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

        // Lecture et split du document
        List<Document> splitDocuments = tokenTextSplitter.split(reader.read());

        // Écriture dans le vector store
        vectorStore.write(splitDocuments);
    }
}
