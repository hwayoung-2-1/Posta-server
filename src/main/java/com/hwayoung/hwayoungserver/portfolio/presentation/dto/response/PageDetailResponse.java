package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioPage;

import java.util.UUID;

public record PageDetailResponse(
        UUID pageId,
        UUID portfolioId,
        int pageNumber,
        String pageImageUrl,
        String extractedText,
        String ownerNote
) {
    public static PageDetailResponse of(PortfolioPage page, String ownerNote) {
        return new PageDetailResponse(
                page.getId(),
                page.getPortfolio().getId(),
                page.getPageNumber(),
                page.getPageImageUrl(),
                page.getExtractedText(),
                ownerNote
        );
    }
}
