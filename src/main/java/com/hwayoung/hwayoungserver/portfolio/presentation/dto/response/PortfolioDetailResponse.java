package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioStatus;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioVisibility;

import java.util.List;
import java.util.UUID;

public record PortfolioDetailResponse(
        UUID portfolioId,
        String title,
        String description,
        PortfolioVisibility visibility,
        PortfolioStatus status,
        PortfolioOwnerResponse owner,
        String thumbnailUrl,
        int pageCount,
        List<String> roles,
        List<String> skills,
        String summary
) {
    public static PortfolioDetailResponse of(Portfolio portfolio, int pageCount, String summary) {
        return new PortfolioDetailResponse(
                portfolio.getId(),
                portfolio.getTitle(),
                portfolio.getDescription(),
                portfolio.getVisibility(),
                portfolio.getStatus(),
                PortfolioOwnerResponse.from(portfolio.getOwner()),
                portfolio.getThumbnailUrl(),
                pageCount,
                portfolio.getRoles().stream().map(role -> role.getName()).toList(),
                portfolio.getSkills().stream().map(skill -> skill.getName()).toList(),
                summary
        );
    }
}
