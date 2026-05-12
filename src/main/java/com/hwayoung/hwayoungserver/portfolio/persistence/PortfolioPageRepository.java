package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioPageRepository extends JpaRepository<PortfolioPage, UUID> {
    List<PortfolioPage> findByPortfolioOrderByPageNumberAsc(Portfolio portfolio);

    Optional<PortfolioPage> findByPortfolioAndPageNumber(Portfolio portfolio, int pageNumber);
}
