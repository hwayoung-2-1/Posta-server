package com.hwayoung.hwayoungserver.portfolio.application;

import com.hwayoung.hwayoungserver.common.ApiException;
import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioContext;
import com.hwayoung.hwayoungserver.portfolio.exception.EmptyPageContextException;
import com.hwayoung.hwayoungserver.portfolio.exception.InvalidPageNumberException;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioContextRepository;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.request.BulkUpsertPageContextRequest;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.BulkUpsertPageContextResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PageContextListItemResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PageContextResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PageContextsResponse;
import com.hwayoung.hwayoungserver.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioPageContextService {
    private final PortfolioPdfService portfolioPdfService;
    private final PortfolioContextRepository portfolioContextRepository;

    @Transactional(readOnly = true)
    public PageContextsResponse getPageContexts(User viewer, UUID portfolioId) {
        Portfolio portfolio = portfolioPdfService.getAccessiblePortfolio(viewer, portfolioId);
        Map<Integer, PortfolioContext> contexts = new HashMap<>();
        portfolioContextRepository.findByPortfolioIdOrderByPageNumberAsc(portfolio.getId())
                .forEach(context -> contexts.put(context.getPageNumber(), context));

        List<PageContextListItemResponse> pages = new ArrayList<>();
        for (int pageNumber = 1; pageNumber <= portfolio.getPageCount(); pageNumber++) {
            PortfolioContext context = contexts.get(pageNumber);
            pages.add(context == null
                    ? PageContextListItemResponse.empty(pageNumber)
                    : PageContextListItemResponse.from(context));
        }
        return new PageContextsResponse(portfolio.getId(), portfolio.getPageCount(), pages);
    }

    @Transactional
    public PageContextResponse upsertPageContext(User owner, UUID portfolioId, int pageNumber, String content) {
        Portfolio portfolio = portfolioPdfService.getOwnedPortfolio(owner, portfolioId);
        validatePageNumber(pageNumber, portfolio.getPageCount());
        validateContent(content);

        PortfolioContext context = portfolioContextRepository.findByPortfolioIdAndPageNumber(portfolio.getId(), pageNumber)
                .orElseGet(() -> new PortfolioContext(portfolio, pageNumber, content));
        context.updateContent(content, null);
        return PageContextResponse.from(portfolioContextRepository.save(context));
    }

    @Transactional
    public BulkUpsertPageContextResponse upsertPageContexts(
            User owner,
            UUID portfolioId,
            BulkUpsertPageContextRequest request
    ) {
        Portfolio portfolio = portfolioPdfService.getOwnedPortfolio(owner, portfolioId);
        if (request == null || request.contexts() == null || request.contexts().isEmpty()) {
            throw ApiException.badRequest("contexts는 필수입니다.");
        }

        Set<Integer> pageNumbers = new HashSet<>();
        for (BulkUpsertPageContextRequest.Item item : request.contexts()) {
            if (item == null || !pageNumbers.add(item.pageNumber())) {
                throw ApiException.badRequest("중복된 pageNumber가 포함되어 있습니다.");
            }
            validatePageNumber(item.pageNumber(), portfolio.getPageCount());
            validateContent(item.content());
        }

        Map<Integer, PortfolioContext> existingContexts = new HashMap<>();
        portfolioContextRepository.findByPortfolioIdOrderByPageNumberAsc(portfolio.getId())
                .forEach(context -> existingContexts.put(context.getPageNumber(), context));

        List<PageContextResponse> responses = new ArrayList<>();
        for (BulkUpsertPageContextRequest.Item item : request.contexts()) {
            PortfolioContext context = existingContexts.getOrDefault(
                    item.pageNumber(),
                    new PortfolioContext(portfolio, item.pageNumber(), item.content())
            );
            context.updateContent(item.content(), null);
            responses.add(PageContextResponse.from(portfolioContextRepository.save(context)));
        }

        return new BulkUpsertPageContextResponse(portfolio.getId(), responses.size(), responses);
    }

    @Transactional
    public void deletePageContext(User owner, UUID portfolioId, int pageNumber) {
        Portfolio portfolio = portfolioPdfService.getOwnedPortfolio(owner, portfolioId);
        validatePageNumber(pageNumber, portfolio.getPageCount());
        portfolioContextRepository.deleteByPortfolioIdAndPageNumber(portfolio.getId(), pageNumber);
    }

    private void validatePageNumber(int pageNumber, int pageCount) {
        if (pageNumber < 1 || pageNumber > pageCount) {
            throw new InvalidPageNumberException(pageNumber, pageCount);
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new EmptyPageContextException();
        }
    }
}
