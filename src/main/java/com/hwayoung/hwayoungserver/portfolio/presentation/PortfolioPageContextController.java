package com.hwayoung.hwayoungserver.portfolio.presentation;

import com.hwayoung.hwayoungserver.portfolio.application.PortfolioPageContextService;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.request.BulkUpsertPageContextRequest;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.request.UpsertPageContextRequest;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.BulkUpsertPageContextResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PageContextResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PageContextsResponse;
import com.hwayoung.hwayoungserver.user.CurrentUserService;
import com.hwayoung.hwayoungserver.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@Tag(name = "챗봇 기능")
@RestController
@RequestMapping("/api/v1/portfolios/{portfolioId}/pages")
@RequiredArgsConstructor
public class PortfolioPageContextController {
    private final CurrentUserService currentUserService;
    private final PortfolioPageContextService pageContextService;

    @GetMapping
    public ResponseEntity<PageContextsResponse> getPageContexts(@PathVariable UUID portfolioId) {
        Optional<User> user = currentUserService.getCurrentUserOptional();
        return ResponseEntity.ok(pageContextService.getPageContexts(user.orElse(null), portfolioId));
    }

    @PutMapping("/{pageNumber}/context")
    public ResponseEntity<PageContextResponse> upsertPageContext(
            @PathVariable UUID portfolioId,
            @PathVariable int pageNumber,
            @RequestBody UpsertPageContextRequest request
    ) {
        User user = currentUserService.getCurrentUser();
        String content = request == null ? null : request.content();
        return ResponseEntity.ok(pageContextService.upsertPageContext(user, portfolioId, pageNumber, content));
    }

    @PutMapping("/contexts")
    public ResponseEntity<BulkUpsertPageContextResponse> upsertPageContexts(
            @PathVariable UUID portfolioId,
            @RequestBody BulkUpsertPageContextRequest request
    ) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(pageContextService.upsertPageContexts(user, portfolioId, request));
    }

    @DeleteMapping("/{pageNumber}/context")
    public ResponseEntity<Void> deletePageContext(
            @PathVariable UUID portfolioId,
            @PathVariable int pageNumber
    ) {
        User user = currentUserService.getCurrentUser();
        pageContextService.deletePageContext(user, portfolioId, pageNumber);
        return ResponseEntity.noContent().build();
    }
}
