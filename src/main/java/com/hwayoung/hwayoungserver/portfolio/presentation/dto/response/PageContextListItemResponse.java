package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioContext;

import java.time.LocalDateTime;
import java.util.UUID;

public record PageContextListItemResponse(
        int pageNumber,
        UUID contextId,
        String content,
        boolean hasContent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PageContextListItemResponse empty(int pageNumber) {
        return new PageContextListItemResponse(pageNumber, null, null, false, null, null);
    }

    public static PageContextListItemResponse from(PortfolioContext context) {
        return new PageContextListItemResponse(
                context.getPageNumber(),
                context.getId(),
                context.getContent(),
                true,
                context.getCreatedAt(),
                context.getUpdatedAt()
        );
    }
}
