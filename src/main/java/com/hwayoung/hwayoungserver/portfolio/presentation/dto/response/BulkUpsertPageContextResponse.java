package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record BulkUpsertPageContextResponse(
        UUID portfolioId,
        int updatedCount,
        List<PageContextResponse> contexts
) {
}
