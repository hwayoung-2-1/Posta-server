package com.hwayoung.hwayoungserver.portfolio.presentation.dto.request;

import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioVisibility;

import java.util.List;
import java.util.UUID;

public record UpdatePortfolioRequest(
        String title,
        String description,
        PortfolioVisibility visibility,
        List<UUID> roleIds,
        List<UUID> skillIds
) {
}
