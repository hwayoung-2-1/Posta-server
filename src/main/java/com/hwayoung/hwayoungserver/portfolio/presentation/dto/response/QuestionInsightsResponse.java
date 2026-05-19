package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record QuestionInsightsResponse(
        UUID portfolioId,
        long totalQuestions,
        List<QuestionInsightItemResponse> clusters,
        int page,
        int size,
        long totalElements
) {
}
