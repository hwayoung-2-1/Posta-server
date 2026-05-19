package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;

import java.time.LocalDateTime;
import java.util.UUID;

public record PortfolioDetailResponse(
        UUID id,
        UUID ownerId,
        String title,
        String description,
        String visibility,
        int pageCount,
        PdfMetadataResponse pdf,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PortfolioDetailResponse from(Portfolio portfolio) {
        return new PortfolioDetailResponse(
                portfolio.getId(),
                portfolio.getOwner().getId(),
                portfolio.getTitle(),
                portfolio.getDescription(),
                portfolio.getVisibility().toJson(),
                portfolio.getPageCount(),
                PdfMetadataResponse.from(portfolio),
                portfolio.getLikeCount(),
                portfolio.getCommentCount(),
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt()
        );
    }

    public static PortfolioDetailResponse of(Portfolio portfolio, int pageCount, String summary) {
        return from(portfolio);
    }
}
