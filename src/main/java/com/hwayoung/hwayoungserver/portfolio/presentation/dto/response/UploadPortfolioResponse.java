package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioStatus;

import java.util.UUID;

public record UploadPortfolioResponse(
        UUID portfolioId,
        PortfolioStatus status,
        String fileUrl,
        String processingStatusUrl
) {
    public static UploadPortfolioResponse of(UUID portfolioId, PortfolioStatus status, String fileUrl) {
        return new UploadPortfolioResponse(
                portfolioId,
                status,
                fileUrl,
                "/api/v1/portfolios/" + portfolioId + "/processing-status"
        );
    }
}
