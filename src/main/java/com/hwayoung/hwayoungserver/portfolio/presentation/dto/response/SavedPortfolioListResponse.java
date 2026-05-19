package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import java.util.List;

public record SavedPortfolioListResponse(
        List<SavedPortfolioListItemResponse> content,
        int page,
        int size,
        long totalElements
) {
}
