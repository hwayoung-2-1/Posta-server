package com.hwayoung.hwayoungserver.portfolio.presentation;

import com.hwayoung.hwayoungserver.portfolio.application.QuestionInsightService;
import com.hwayoung.hwayoungserver.portfolio.domain.type.QuestionClusterStatus;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.request.UpdateQuestionInsightStatusRequest;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.QuestionInsightDetailResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.QuestionInsightsResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.ReindexResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.UpdateQuestionInsightStatusResponse;
import com.hwayoung.hwayoungserver.user.CurrentUserService;
import com.hwayoung.hwayoungserver.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolios/{portfolioId}/question-insights")
@RequiredArgsConstructor
public class QuestionInsightController {
    private final CurrentUserService currentUserService;
    private final QuestionInsightService questionInsightService;

    @GetMapping
    public ResponseEntity<QuestionInsightsResponse> list(
            @PathVariable UUID portfolioId,
            @RequestParam(required = false) QuestionClusterStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(questionInsightService.list(user, portfolioId, status, page, size));
    }

    @GetMapping("/{clusterId}")
    public ResponseEntity<QuestionInsightDetailResponse> detail(
            @PathVariable UUID portfolioId,
            @PathVariable UUID clusterId
    ) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(questionInsightService.detail(user, portfolioId, clusterId));
    }

    @PatchMapping("/{clusterId}")
    public ResponseEntity<UpdateQuestionInsightStatusResponse> updateStatus(
            @PathVariable UUID portfolioId,
            @PathVariable UUID clusterId,
            @RequestBody UpdateQuestionInsightStatusRequest request
    ) {
        User user = currentUserService.getCurrentUser();
        QuestionClusterStatus status = request == null ? null : request.status();
        return ResponseEntity.ok(questionInsightService.updateStatus(user, portfolioId, clusterId, status));
    }

    @PostMapping("/rebuild")
    public ResponseEntity<ReindexResponse> rebuild(@PathVariable UUID portfolioId) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(questionInsightService.rebuild(user, portfolioId));
    }
}
