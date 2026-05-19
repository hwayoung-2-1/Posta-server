package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioPage;

import java.util.UUID;

public record PageListItemResponse(
        UUID pageId,
        int pageNumber,
        String pageImageUrl,
        boolean hasOwnerNote
) {
    public static PageListItemResponse of(PortfolioPage page, boolean hasOwnerNote) {
        return new PageListItemResponse(page.getId(), page.getPageNumber(), page.getPageImageUrl(), hasOwnerNote);
    }
}
