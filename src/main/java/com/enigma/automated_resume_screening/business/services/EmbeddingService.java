package com.enigma.automated_resume_screening.business.services;

public interface EmbeddingService {
    float[] generateEmbedding(String text);
}
