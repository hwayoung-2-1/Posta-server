package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioSummary;
import com.hwayoung.hwayoungserver.portfolio.domain.type.SummaryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface PortfolioSummaryRepository extends JpaRepository<PortfolioSummary, UUID> {
    Optional<PortfolioSummary> findFirstByPortfolioAndSummaryType(Portfolio portfolio, SummaryType summaryType);

    List<PortfolioSummary> findByPortfolio(Portfolio portfolio);
}
