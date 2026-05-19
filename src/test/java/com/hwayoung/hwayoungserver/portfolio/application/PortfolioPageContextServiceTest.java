package com.hwayoung.hwayoungserver.portfolio.application;

import com.hwayoung.hwayoungserver.common.ApiException;
import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioContext;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioVisibility;
import com.hwayoung.hwayoungserver.portfolio.exception.PortfolioAccessDeniedException;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioContextRepository;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.request.BulkUpsertPageContextRequest;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.BulkUpsertPageContextResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PageContextResponse;
import com.hwayoung.hwayoungserver.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioPageContextServiceTest {
    @Mock
    private PortfolioPdfService portfolioPdfService;
    @Mock
    private PortfolioContextRepository portfolioContextRepository;

    private PortfolioPageContextService pageContextService;
    private User owner;
    private Portfolio portfolio;
    private UUID portfolioId;

    @BeforeEach
    void setUp() {
        pageContextService = new PortfolioPageContextService(portfolioPdfService, portfolioContextRepository);
        owner = user(UUID.randomUUID(), "owner@example.com");
        portfolioId = UUID.randomUUID();
        portfolio = new Portfolio(owner, "title", "description", PortfolioVisibility.PRIVATE);
        ReflectionTestUtils.setField(portfolio, "id", portfolioId);
        portfolio.updatePdfMetadata("object-key", "portfolio.pdf", "application/pdf", 1024L, 3);
    }

    @Test
    @DisplayName("pageNumber가 0이면 실패한다")
    void pageNumberZeroFails() {
        when(portfolioPdfService.getOwnedPortfolio(owner, portfolioId)).thenReturn(portfolio);

        assertThatThrownBy(() -> pageContextService.upsertPageContext(owner, portfolioId, 0, "content"))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("pageNumber가 pageCount보다 크면 실패한다")
    void pageNumberGreaterThanPageCountFails() {
        when(portfolioPdfService.getOwnedPortfolio(owner, portfolioId)).thenReturn(portfolio);

        assertThatThrownBy(() -> pageContextService.upsertPageContext(owner, portfolioId, 4, "content"))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 context를 수정하면 403을 반환한다")
    void nonOwnerUpsertFails() {
        when(portfolioPdfService.getOwnedPortfolio(owner, portfolioId))
                .thenThrow(new PortfolioAccessDeniedException("포트폴리오 작성자만 요청할 수 있습니다."));

        assertThatThrownBy(() -> pageContextService.upsertPageContext(owner, portfolioId, 1, "content"))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("context 최초 저장 시 insert한다")
    void insertPageContext() {
        when(portfolioPdfService.getOwnedPortfolio(owner, portfolioId)).thenReturn(portfolio);
        when(portfolioContextRepository.findByPortfolioIdAndPageNumber(portfolioId, 1)).thenReturn(Optional.empty());
        when(portfolioContextRepository.save(any(PortfolioContext.class))).thenAnswer(invocation -> {
            PortfolioContext context = invocation.getArgument(0);
            ReflectionTestUtils.setField(context, "id", UUID.randomUUID());
            return context;
        });

        PageContextResponse response = pageContextService.upsertPageContext(owner, portfolioId, 1, "1페이지 설명");

        assertThat(response.portfolioId()).isEqualTo(portfolioId);
        assertThat(response.pageNumber()).isEqualTo(1);
        assertThat(response.content()).isEqualTo("1페이지 설명");
    }

    @Test
    @DisplayName("context 재저장 시 update한다")
    void updatePageContext() {
        PortfolioContext context = new PortfolioContext(portfolio, 2, "old");
        UUID contextId = UUID.randomUUID();
        ReflectionTestUtils.setField(context, "id", contextId);
        when(portfolioPdfService.getOwnedPortfolio(owner, portfolioId)).thenReturn(portfolio);
        when(portfolioContextRepository.findByPortfolioIdAndPageNumber(portfolioId, 2)).thenReturn(Optional.of(context));
        when(portfolioContextRepository.save(any(PortfolioContext.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PageContextResponse response = pageContextService.upsertPageContext(owner, portfolioId, 2, "new");

        assertThat(response.id()).isEqualTo(contextId);
        assertThat(response.content()).isEqualTo("new");
    }

    @Test
    @DisplayName("bulk upsert를 하나의 요청으로 처리한다")
    void bulkUpsertSuccess() {
        when(portfolioPdfService.getOwnedPortfolio(owner, portfolioId)).thenReturn(portfolio);
        when(portfolioContextRepository.findByPortfolioIdOrderByPageNumberAsc(portfolioId)).thenReturn(List.of());
        when(portfolioContextRepository.save(any(PortfolioContext.class))).thenAnswer(invocation -> {
            PortfolioContext context = invocation.getArgument(0);
            ReflectionTestUtils.setField(context, "id", UUID.randomUUID());
            return context;
        });

        BulkUpsertPageContextRequest request = new BulkUpsertPageContextRequest(List.of(
                new BulkUpsertPageContextRequest.Item(1, "1페이지 설명"),
                new BulkUpsertPageContextRequest.Item(2, "2페이지 설명"),
                new BulkUpsertPageContextRequest.Item(3, "3페이지 설명")
        ));

        BulkUpsertPageContextResponse response = pageContextService.upsertPageContexts(owner, portfolioId, request);

        assertThat(response.portfolioId()).isEqualTo(portfolioId);
        assertThat(response.updatedCount()).isEqualTo(3);
        assertThat(response.contexts()).extracting(PageContextResponse::pageNumber).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("bulk 요청에 중복 pageNumber가 있으면 400을 반환한다")
    void bulkDuplicatePageNumberFails() {
        when(portfolioPdfService.getOwnedPortfolio(owner, portfolioId)).thenReturn(portfolio);
        BulkUpsertPageContextRequest request = new BulkUpsertPageContextRequest(List.of(
                new BulkUpsertPageContextRequest.Item(1, "1페이지 설명"),
                new BulkUpsertPageContextRequest.Item(1, "중복 설명")
        ));

        assertThatThrownBy(() -> pageContextService.upsertPageContexts(owner, portfolioId, request))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private User user(UUID id, String email) {
        User user = new User(email, "password", email);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
