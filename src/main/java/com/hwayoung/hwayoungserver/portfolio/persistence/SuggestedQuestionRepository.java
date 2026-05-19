package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.SuggestedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SuggestedQuestionRepository extends JpaRepository<SuggestedQuestion, UUID> {
    List<SuggestedQuestion> findByPortfolioOrderByDisplayOrderAsc(Portfolio portfolio);
}
