package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.SavedPortfolio;

import java.time.LocalDateTime;
import java.util.UUID;

public record SavedPortfolioListItemResponse(
        UUID portfolioId,
        String title,
        String ownerName,
        String thumbnailUrl,
        LocalDateTime savedAt
) {
    public static SavedPortfolioListItemResponse from(SavedPortfolio savedPortfolio) {
        return new SavedPortfolioListItemResponse(
                savedPortfolio.getPortfolio().getId(),
                savedPortfolio.getPortfolio().getTitle(),
                savedPortfolio.getPortfolio().getOwner().getName(),
                savedPortfolio.getPortfolio().getThumbnailUrl(),
                savedPortfolio.getCreatedAt()
        );
    }
}
