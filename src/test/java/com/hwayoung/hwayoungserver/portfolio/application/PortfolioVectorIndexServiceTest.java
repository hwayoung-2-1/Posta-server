package com.hwayoung.hwayoungserver.portfolio.application;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioVisibility;
import com.hwayoung.hwayoungserver.portfolio.persistence.PageOwnerNoteRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioContextRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioPageRepository;
import com.hwayoung.hwayoungserver.taxonomy.domain.model.RoleEntity;
import com.hwayoung.hwayoungserver.taxonomy.domain.model.SkillEntity;
import com.hwayoung.hwayoungserver.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioVectorIndexServiceTest {
    @Mock
    private ObjectProvider<VectorStore> vectorStoreProvider;
    @Mock
    private VectorStore vectorStore;
    @Mock
    private PortfolioPageRepository portfolioPageRepository;
    @Mock
    private PageOwnerNoteRepository pageOwnerNoteRepository;
    @Mock
    private PortfolioContextRepository portfolioContextRepository;

    private PortfolioVectorIndexService vectorIndexService;
    private UUID portfolioId;
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        PortfolioAiProperties properties = new PortfolioAiProperties();
        properties.setChunkSize(1200);
        properties.setChunkOverlap(160);

        vectorIndexService = new PortfolioVectorIndexService(
                vectorStoreProvider,
                portfolioPageRepository,
                pageOwnerNoteRepository,
                portfolioContextRepository,
                properties
        );

        portfolioId = UUID.randomUUID();
        User owner = new User("owner@example.com", "password", "김하영");
        ReflectionTestUtils.setField(owner, "id", UUID.randomUUID());
        portfolio = new Portfolio(owner, "백엔드 포트폴리오", "주문 시스템 프로젝트", PortfolioVisibility.PUBLIC);
        ReflectionTestUtils.setField(portfolio, "id", portfolioId);
        portfolio.getRoles().add(new RoleEntity("Backend"));
        portfolio.getSkills().add(new SkillEntity("Spring Boot"));
        portfolio.getSkills().add(new SkillEntity("PostgreSQL"));
    }

    @Test
    @DisplayName("색인 시 작성자와 포트폴리오 메타정보를 RAG 문서에 포함한다")
    void reindexIncludesOwnerProfileDocument() {
        when(vectorStoreProvider.getIfAvailable()).thenReturn(vectorStore);
        when(portfolioPageRepository.findByPortfolioOrderByPageNumberAsc(portfolio)).thenReturn(List.of());
        when(portfolioContextRepository.findByPortfolioIdOrderByPageNumberAsc(portfolioId)).thenReturn(List.of());

        int indexedCount = vectorIndexService.reindex(portfolio);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Document>> documentsCaptor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).delete(PortfolioVectorIndexService.portfolioFilter(portfolioId));
        verify(vectorStore).add(documentsCaptor.capture());

        List<Document> documents = documentsCaptor.getValue();
        assertThat(indexedCount).isEqualTo(1);
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getMetadata())
                .containsEntry("sourceType", "OWNER_PROFILE")
                .containsEntry("portfolioId", portfolioId.toString());
        assertThat(documents.get(0).getText())
                .contains("김하영")
                .contains("Backend")
                .contains("Spring Boot")
                .contains("PostgreSQL");
    }
}
