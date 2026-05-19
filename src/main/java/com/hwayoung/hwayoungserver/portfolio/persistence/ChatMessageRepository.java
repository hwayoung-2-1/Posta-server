package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatMessage;
import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatSession;
import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.type.ChatMessageRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findByChatSessionOrderByCreatedAtAsc(ChatSession chatSession);

    List<ChatMessage> findByChatSessionPortfolioAndRoleAndAnalyzableTrueOrderByCreatedAtAsc(
            Portfolio portfolio,
            ChatMessageRole role
    );

    long countByChatSessionPortfolioAndRoleAndAnalyzableTrue(Portfolio portfolio, ChatMessageRole role);
}
