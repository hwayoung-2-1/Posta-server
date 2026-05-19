package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.portfolio.domain.model.ChatSession;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateChatSessionResponse(
        UUID chatSessionId,
        UUID portfolioId,
        UUID viewerUserId,
        LocalDateTime createdAt
) {
    public static CreateChatSessionResponse from(ChatSession chatSession) {
        return new CreateChatSessionResponse(
                chatSession.getId(),
                chatSession.getPortfolio().getId(),
                chatSession.getViewer().getId(),
                chatSession.getCreatedAt()
        );
    }
}
