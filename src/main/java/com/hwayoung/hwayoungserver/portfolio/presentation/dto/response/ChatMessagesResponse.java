package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record ChatMessagesResponse(
        UUID chatSessionId,
        List<ChatMessageItemResponse> messages
) {
}
