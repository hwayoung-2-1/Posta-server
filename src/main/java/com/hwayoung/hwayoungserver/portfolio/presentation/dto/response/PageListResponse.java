package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record PageListResponse(
        UUID portfolioId,
        List<PageListItemResponse> pages
) {
}
