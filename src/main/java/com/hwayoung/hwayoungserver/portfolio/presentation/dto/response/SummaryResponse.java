package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.type.SummaryType;

import java.util.UUID;

public record SummaryResponse(
        UUID portfolioId,
        SummaryType summaryType,
        String content
) {
}
