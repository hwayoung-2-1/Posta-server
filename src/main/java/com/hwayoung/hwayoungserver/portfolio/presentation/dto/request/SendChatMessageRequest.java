package com.hwayoung.hwayoungserver.portfolio.presentation.dto.request;

public record SendChatMessageRequest(
        String message,
        Integer currentPage
) {
}
