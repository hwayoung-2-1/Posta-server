package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioContext;

import java.time.LocalDateTime;
import java.util.UUID;

public record PageContextResponse(
        UUID id,
        UUID portfolioId,
        int pageNumber,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PageContextResponse from(PortfolioContext context) {
        return new PageContextResponse(
                context.getId(),
                context.getPortfolio().getId(),
                context.getPageNumber(),
                context.getContent(),
                context.getCreatedAt(),
                context.getUpdatedAt()
        );
    }
}
