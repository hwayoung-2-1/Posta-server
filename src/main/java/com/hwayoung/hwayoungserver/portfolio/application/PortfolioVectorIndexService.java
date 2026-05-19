package com.hwayoung.hwayoungserver.portfolio.application;

import com.hwayoung.hwayoungserver.common.ApiException;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PageOwnerNote;
import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioContext;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioPage;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioSummary;
import com.hwayoung.hwayoungserver.portfolio.persistence.PageOwnerNoteRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioContextRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioPageRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioVectorIndexService {
    private static final String SOURCE_PDF_TEXT = "PDF_TEXT";
    private static final String SOURCE_OWNER_NOTE = "OWNER_NOTE";
    private static final String SOURCE_PAGE_CONTEXT = "PAGE_CONTEXT";
    private static final String SOURCE_SUMMARY = "SUMMARY";

    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final PortfolioPageRepository portfolioPageRepository;
    private final PageOwnerNoteRepository pageOwnerNoteRepository;
    private final PortfolioContextRepository portfolioContextRepository;
    private final PortfolioSummaryRepository portfolioSummaryRepository;
    private final PortfolioAiProperties properties;

    @Transactional
    public int reindex(Portfolio portfolio) {
        VectorStore vectorStore = vectorStore();
        vectorStore.delete(portfolioFilter(portfolio.getId()));

        List<Document> documents = new ArrayList<>();
        for (PortfolioPage page : portfolioPageRepository.findByPortfolioOrderByPageNumberAsc(portfolio)) {
            addChunks(
                    documents,
                    portfolio,
                    page.getPageNumber(),
                    SOURCE_PDF_TEXT,
                    page.getId().toString(),
                    page.getExtractedText()
            );
            pageOwnerNoteRepository.findByPortfolioPage(page)
                    .ifPresent(note -> addOwnerNoteChunks(documents, portfolio, page, note));
        }

        for (PortfolioContext context : portfolioContextRepository.findByPortfolioIdOrderByPageNumberAsc(portfolio.getId())) {
            addChunks(
                    documents,
                    portfolio,
                    context.getPageNumber(),
                    SOURCE_PAGE_CONTEXT,
                    context.getId().toString(),
                    context.getContent()
            );
        }

        for (PortfolioSummary summary : portfolioSummaryRepository.findByPortfolio(portfolio)) {
            addChunks(documents, portfolio, null, SOURCE_SUMMARY, summary.getSummaryType().name(), summary.getContent());
        }

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
        return documents.size();
    }

    private void addOwnerNoteChunks(
            List<Document> documents,
            Portfolio portfolio,
            PortfolioPage page,
            PageOwnerNote note
    ) {
        addChunks(
                documents,
                portfolio,
                page.getPageNumber(),
                SOURCE_OWNER_NOTE,
                note.getId().toString(),
                note.getContent()
        );
    }

    private void addChunks(
            List<Document> documents,
            Portfolio portfolio,
            Integer pageNumber,
            String sourceType,
            String sourceId,
            String content
    ) {
        if (content == null || content.isBlank()) {
            return;
        }
        List<String> chunks = split(content);
        for (int i = 0; i < chunks.size(); i++) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("portfolioId", portfolio.getId().toString());
            metadata.put("portfolioTitle", portfolio.getTitle());
            metadata.put("sourceType", sourceType);
            metadata.put("sourceId", sourceId);
            metadata.put("chunkIndex", i);
            if (pageNumber != null) {
                metadata.put("pageNumber", pageNumber);
            }

            String documentId = UUID.randomUUID().toString();
            documents.add(new Document(documentId, formatContent(portfolio, pageNumber, sourceType, chunks.get(i)), metadata));
        }
    }

    private List<String> split(String content) {
        String normalized = content.replace("\r\n", "\n").trim();
        int chunkSize = properties.getChunkSize();
        int overlap = Math.min(properties.getChunkOverlap(), chunkSize / 2);
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(start + chunkSize, normalized.length());
            chunks.add(normalized.substring(start, end).trim());
            if (end >= normalized.length()) {
                break;
            }
            start = Math.max(0, end - overlap);
        }
        return chunks;
    }

    private String formatContent(Portfolio portfolio, Integer pageNumber, String sourceType, String content) {
        String pageText = pageNumber == null ? "전체" : pageNumber.toString();
        return """
                포트폴리오 제목: %s
                페이지: %s
                출처: %s
                내용:
                %s
                """.formatted(portfolio.getTitle(), pageText, sourceType, content);
    }

    private VectorStore vectorStore() {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            throw ApiException.conflict(
                    "AI_VECTOR_STORE_NOT_CONFIGURED",
                    "VectorRAG를 사용하려면 EmbeddingModel과 PgVector VectorStore 설정이 필요합니다."
            );
        }
        return vectorStore;
    }

    static String portfolioFilter(UUID portfolioId) {
        return "portfolioId == '" + portfolioId + "'";
    }
}
