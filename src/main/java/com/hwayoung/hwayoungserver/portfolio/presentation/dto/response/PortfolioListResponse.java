package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import java.util.List;

public record PortfolioListResponse(
        List<PortfolioListItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
