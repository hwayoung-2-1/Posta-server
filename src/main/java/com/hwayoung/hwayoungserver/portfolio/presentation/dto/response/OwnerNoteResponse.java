package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioPage;

import java.util.UUID;

public record OwnerNoteResponse(
        UUID pageId,
        int pageNumber,
        String ownerNote,
        String reindexStatus
) {
    public static OwnerNoteResponse of(PortfolioPage page, String ownerNote) {
        return new OwnerNoteResponse(page.getId(), page.getPageNumber(), ownerNote, "SCHEDULED");
    }
}
