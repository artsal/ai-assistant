package com.github.artsal.ai.assistant.backend.rag;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    private List<String> chunks = new ArrayList<>();

    public void addDocument(String content) {
        this.chunks = splitIntoChunks(clean(content));
    }

    public String getRelevantChunk(String query) {
        String bestChunk = "";

        int maxScore = 0;

        for (String chunk : chunks) {
            int score = score(chunk, query);

            if (score > maxScore) {
                maxScore = score;
                bestChunk = chunk;
            }
        }

        return bestChunk;
    }

    private List<String> splitIntoChunks(String text) {
        List<String> result = new ArrayList<>();

        int chunkSize = 500;

        for (int i = 0; i < text.length(); i += chunkSize) {
            result.add(text.substring(i, Math.min(text.length(), i + chunkSize)));
        }

        return result;
    }

    private int score(String chunk, String query) {
        int score = 0;

        String[] words = query.toLowerCase().split("\\s+");

        for (String word : words) {
            if (chunk.toLowerCase().contains(word)) {
                score++;
            }
        }

        return score;
    }

    private String clean(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    public void clear() {
        this.chunks.clear();
    }
}