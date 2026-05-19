package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    @EntityGraph(attributePaths = {"portfolio", "portfolio.owner", "viewer"})
    Optional<ChatSession> findById(UUID id);
}
