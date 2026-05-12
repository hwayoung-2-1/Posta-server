package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record PageContextsResponse(
        UUID portfolioId,
        int pageCount,
        List<PageContextListItemResponse> pages
) {
}
