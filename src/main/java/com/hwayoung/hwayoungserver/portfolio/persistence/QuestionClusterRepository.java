package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.QuestionCluster;
import com.hwayoung.hwayoungserver.portfolio.domain.type.QuestionCategory;
import com.hwayoung.hwayoungserver.portfolio.domain.type.QuestionClusterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionClusterRepository extends JpaRepository<QuestionCluster, UUID> {
    Page<QuestionCluster> findByPortfolioOrderByLastAskedAtDesc(Portfolio portfolio, Pageable pageable);

    Page<QuestionCluster> findByPortfolioAndStatusOrderByLastAskedAtDesc(
            Portfolio portfolio,
            QuestionClusterStatus status,
            Pageable pageable
    );

    Optional<QuestionCluster> findByPortfolioAndId(Portfolio portfolio, UUID id);

    Optional<QuestionCluster> findFirstByPortfolioAndCategoryAndStatusOrderByLastAskedAtDesc(
            Portfolio portfolio,
            QuestionCategory category,
            QuestionClusterStatus status
    );

    long countByPortfolio(Portfolio portfolio);

    void deleteByPortfolio(Portfolio portfolio);
}
