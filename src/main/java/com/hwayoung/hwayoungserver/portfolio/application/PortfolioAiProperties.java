package com.hwayoung.hwayoungserver.portfolio.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai.rag")
public class PortfolioAiProperties {
    private int topK = 6;
    private double similarityThreshold = 0.55;
    private int maxContextCharacters = 7000;
    private int chunkSize = 1200;
    private int chunkOverlap = 160;

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = Math.max(1, topK);
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public int getMaxContextCharacters() {
        return maxContextCharacters;
    }

    public void setMaxContextCharacters(int maxContextCharacters) {
        this.maxContextCharacters = Math.max(1000, maxContextCharacters);
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = Math.max(300, chunkSize);
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    public void setChunkOverlap(int chunkOverlap) {
        this.chunkOverlap = Math.max(0, chunkOverlap);
    }
}
