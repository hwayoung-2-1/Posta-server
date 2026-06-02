package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;

import java.util.List;
import java.util.UUID;

public record PortfolioListItemResponse(
        UUID portfolioId,
        String title,
        String ownerName,
        String thumbnailUrl,
        List<String> roles,
        List<String> skills,
        boolean saved
) {
    public static PortfolioListItemResponse of(Portfolio portfolio, boolean saved) {
        return of(portfolio, portfolio.getThumbnailUrl(), saved);
    }

    public static PortfolioListItemResponse of(Portfolio portfolio, String thumbnailUrl, boolean saved) {
        return new PortfolioListItemResponse(
                portfolio.getId(),
                portfolio.getTitle(),
                portfolio.getOwner().getName(),
                thumbnailUrl,
                portfolio.getRoles().stream().map(role -> role.getName()).toList(),
                portfolio.getSkills().stream().map(skill -> skill.getName()).toList(),
                saved
        );
    }
}
