package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioContextRepository extends JpaRepository<PortfolioContext, UUID> {
    List<PortfolioContext> findByPortfolioIdOrderByPageNumberAsc(UUID portfolioId);

    Optional<PortfolioContext> findByPortfolioIdAndPageNumber(UUID portfolioId, int pageNumber);

    boolean existsByPortfolioIdAndPageNumber(UUID portfolioId, int pageNumber);

    void deleteByPortfolioIdAndPageNumber(UUID portfolioId, int pageNumber);

    void deleteByPortfolioId(UUID portfolioId);
}
