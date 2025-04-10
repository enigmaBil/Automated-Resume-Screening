package com.enigma.automated_resume_screening.business.services;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {
    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    public String extractDataFromLLM(String query) {
        List<Document> documents = vectorStore.similaritySearch(query);

        String systemMessageTemplate = """
				Answer the following questions based solely on the CONTEXT provided.
				if the answer is not in the CONTEXT, answer “I don't know”.
				CONTEXT:
					{CONTEXT}
				""";
        String context = documents.stream().map(Document::toString).collect(Collectors.joining());

        Message systemMessage = new SystemPromptTemplate(systemMessageTemplate).createMessage(Map.of("CONTEXT", context = context.length() > 5000 ? context.substring(0, 5000) : context));

        UserMessage userMessage = new UserMessage(query);

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        OllamaApi ollamaApi = new OllamaApi();
        OllamaOptions ollamaOptions = OllamaOptions.builder().model("llama3").temperature(0D).build();
        ToolCallingManager toolCallingManager = ToolCallingManager.builder().observationRegistry(ObservationRegistry.NOOP).build();
        ModelManagementOptions modelManagementOptions = ModelManagementOptions.builder().maxRetries(1).build();
        ObservationRegistry observationRegistry =	ObservationRegistry.NOOP;
        OllamaChatModel ollamaChatModel = new OllamaChatModel(ollamaApi, ollamaOptions, toolCallingManager, observationRegistry, modelManagementOptions);
        ChatResponse chatResponse = ollamaChatModel.call(prompt);
        System.out.println(chatResponse);
        return chatResponse.getResult().getOutput().getText();
    }

    public void textEmbedding(Resource[] pdfResources) {
        jdbcTemplate.update("delete from vector_store");
        //data ingestion and chuncking
        List<Document> splitDocuments = new ArrayList<>();
        for (Resource pdfResource : pdfResources) {
            PdfDocumentReaderConfig pdfDocumentReaderConfig = PdfDocumentReaderConfig.defaultConfig();
            PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource, pdfDocumentReaderConfig);
            TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

            // Ajoute correctement tous les documents obtenus après le split
            splitDocuments.addAll(tokenTextSplitter.split(reader.read()));
        }
        //System.out.println(splitDocuments);
        vectorStore.write(splitDocuments);

    }
}
