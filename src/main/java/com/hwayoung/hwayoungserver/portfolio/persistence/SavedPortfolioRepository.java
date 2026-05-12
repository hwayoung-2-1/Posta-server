package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.SavedPortfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.SavedPortfolioId;
import com.hwayoung.hwayoungserver.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedPortfolioRepository extends JpaRepository<SavedPortfolio, SavedPortfolioId> {
    boolean existsByUserAndPortfolio(User user, Portfolio portfolio);

    void deleteByUserAndPortfolio(User user, Portfolio portfolio);

    Page<SavedPortfolio> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
