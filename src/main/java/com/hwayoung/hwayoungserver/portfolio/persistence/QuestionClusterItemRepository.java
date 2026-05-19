package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.QuestionCluster;
import com.hwayoung.hwayoungserver.portfolio.domain.model.QuestionClusterItem;
import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionClusterItemRepository extends JpaRepository<QuestionClusterItem, UUID> {
    List<QuestionClusterItem> findByQuestionClusterOrderByCreatedAtDesc(QuestionCluster questionCluster);

    void deleteByQuestionClusterPortfolio(Portfolio portfolio);
}
