package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatMessage;
import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatMessageSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ChatMessageSourceRepository extends JpaRepository<ChatMessageSource, UUID> {
    List<ChatMessageSource> findByChatMessageIn(Collection<ChatMessage> chatMessages);

    List<ChatMessageSource> findByChatMessageOrderByCreatedAtAsc(ChatMessage chatMessage);
}
