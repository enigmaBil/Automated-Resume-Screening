package com.enigma.automated_resume_screening.business.services;

public interface EmbeddingStorageService {
    void storeEmbedding(String content, float[] embedding);
}
