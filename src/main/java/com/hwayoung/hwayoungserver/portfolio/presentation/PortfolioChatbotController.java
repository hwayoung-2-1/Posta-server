package com.hwayoung.hwayoungserver.portfolio.presentation;

import com.hwayoung.hwayoungserver.portfolio.application.PortfolioChatbotService;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.request.SendChatMessageRequest;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.ChatMessagesResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.CreateChatSessionResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.SendChatMessageResponse;
import com.hwayoung.hwayoungserver.user.CurrentUserService;
import com.hwayoung.hwayoungserver.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "챗봇 기능")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PortfolioChatbotController {
    private final CurrentUserService currentUserService;
    private final PortfolioChatbotService portfolioChatbotService;

    @Operation(summary = "ChatBot 생성 API", description = "viwer가 포토폴리오의 portfolier를 볼때 사용하는 AI 생성 API")
    @PostMapping("/portfolios/{portfolioId}/chat-sessions")
    public ResponseEntity<CreateChatSessionResponse> createSession(@PathVariable UUID portfolioId) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioChatbotService.createSession(user, portfolioId));
    }

    @Operation(summary = "AI와의 대화 세션 조회", description = "")
    @GetMapping("/chat-sessions/{chatSessionId}/messages")
    public ResponseEntity<ChatMessagesResponse> messages(@PathVariable UUID chatSessionId) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(portfolioChatbotService.messages(user, chatSessionId));
    }

    @Operation(summary = "메시지 세션 생성 API")
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
