package com.hwayoung.hwayoungserver.portfolio.presentation;

import com.hwayoung.hwayoungserver.portfolio.application.PortfolioChatbotService;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.request.SendChatMessageRequest;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.ChatMessagesResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.CreateChatSessionResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.SendChatMessageResponse;
import com.hwayoung.hwayoungserver.user.CurrentUserService;
import com.hwayoung.hwayoungserver.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PortfolioChatbotController {
    private final CurrentUserService currentUserService;
    private final PortfolioChatbotService portfolioChatbotService;

    @PostMapping("/portfolios/{portfolioId}/chat-sessions")
    public ResponseEntity<CreateChatSessionResponse> createSession(@PathVariable UUID portfolioId) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioChatbotService.createSession(user, portfolioId));
    }

    @GetMapping("/chat-sessions/{chatSessionId}/messages")
    public ResponseEntity<ChatMessagesResponse> messages(@PathVariable UUID chatSessionId) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(portfolioChatbotService.messages(user, chatSessionId));
    }

    @PostMapping("/chat-sessions/{chatSessionId}/messages")
    public ResponseEntity<SendChatMessageResponse> sendMessage(
            @PathVariable UUID chatSessionId,
            @RequestBody SendChatMessageRequest request
    ) {
        User user = currentUserService.getCurrentUser();
        String message = request == null ? null : request.message();
        Integer currentPage = request == null ? null : request.currentPage();
        return ResponseEntity.ok(portfolioChatbotService.sendMessage(user, chatSessionId, message, currentPage));
    }
}
