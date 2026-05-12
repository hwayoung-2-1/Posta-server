package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioStatus;

import java.util.List;
import java.util.UUID;

public record ProcessingStatusResponse(
        UUID portfolioId,
        PortfolioStatus status,
        List<ProcessingStepResponse> steps,
        String failureReason
) {
}
