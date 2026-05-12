package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PublishPortfolioResponse(
        UUID portfolioId,
        PortfolioStatus status,
        String publicSlug,
        LocalDateTime publishedAt
) {
    public static PublishPortfolioResponse from(Portfolio portfolio) {
        return new PublishPortfolioResponse(
                portfolio.getId(),
                portfolio.getStatus(),
                portfolio.getPublicSlug(),
                portfolio.getPublishedAt()
        );
    }
}
