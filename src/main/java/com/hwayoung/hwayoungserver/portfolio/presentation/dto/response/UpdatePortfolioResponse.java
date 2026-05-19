package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioStatus;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioVisibility;

import java.util.UUID;

public record UpdatePortfolioResponse(
        UUID portfolioId,
        String title,
        PortfolioVisibility visibility,
        PortfolioStatus status
) {
    public static UpdatePortfolioResponse from(Portfolio portfolio) {
        return new UpdatePortfolioResponse(
                portfolio.getId(),
                portfolio.getTitle(),
                portfolio.getVisibility(),
                portfolio.getStatus()
        );
    }
}
