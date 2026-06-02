package com.hwayoung.hwayoungserver.portfolio.presentation;

import com.hwayoung.hwayoungserver.portfolio.application.PortfolioService;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioVisibility;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.request.OwnerNoteRequest;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.OwnerNoteResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PageDetailResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PortfolioListResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.ProcessingStatusResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PublishPortfolioResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.ReindexResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.SavedPortfolioListResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.SuggestedQuestionsResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.SummaryResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.request.UpdatePortfolioRequest;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.UpdatePortfolioResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.UploadPortfolioResponse;
import com.hwayoung.hwayoungserver.user.CurrentUserService;
import com.hwayoung.hwayoungserver.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PortfolioController {
    private final CurrentUserService currentUserService;
    private final PortfolioService portfolioService;

    @PostMapping(value = "/portfolios", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadPortfolioResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) List<UUID> roleIds,
            @RequestParam(required = false) List<UUID> skillIds
    ) {
        User user = currentUserService.getCurrentUser();
        UploadPortfolioResponse response = portfolioService.upload(
                user,
                file,
                title,
                description,
                PortfolioVisibility.from(visibility),
                roleIds,
                skillIds
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/portfolios")
    public ResponseEntity<PortfolioListResponse> portfolios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String keyword
    ) {
        Optional<User> user = currentUserService.getCurrentUserOptional();
        return ResponseEntity.ok(portfolioService.list(user.orElse(null), page, size, role, skill, name, keyword));
    }

    @PatchMapping("/portfolios/{portfolioId}")
    public ResponseEntity<UpdatePortfolioResponse> update(
            @PathVariable UUID portfolioId,
            @RequestBody UpdatePortfolioRequest request
    ) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(portfolioService.update(user, portfolioId, request));
    }

    @PostMapping("/portfolios/{portfolioId}/publish")
    public ResponseEntity<PublishPortfolioResponse> publish(@PathVariable UUID portfolioId) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(portfolioService.publish(user, portfolioId));
    }

    @GetMapping("/portfolios/{portfolioId}/processing-status")
    public ResponseEntity<ProcessingStatusResponse> processingStatus(@PathVariable UUID portfolioId) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(portfolioService.processingStatus(user, portfolioId));
    }

    @GetMapping("/portfolios/{portfolioId}/pages/{pageNumber}")
    public ResponseEntity<PageDetailResponse> pageDetail(
            @PathVariable UUID portfolioId,
            @PathVariable int pageNumber
    ) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(portfolioService.pageDetail(user, portfolioId, pageNumber));
    }

    @PatchMapping("/portfolios/{portfolioId}/pages/{pageNumber}/owner-note")
    public ResponseEntity<OwnerNoteResponse> saveOwnerNote(
            @PathVariable UUID portfolioId,
            @PathVariable int pageNumber,
            @RequestBody OwnerNoteRequest request
    ) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(portfolioService.saveOwnerNote(user, portfolioId, pageNumber, request.content()));
    }

    @PostMapping("/portfolios/{portfolioId}/reindex")
    public ResponseEntity<ReindexResponse> reindex(@PathVariable UUID portfolioId) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(portfolioService.reindex(user, portfolioId));
    }

    @GetMapping("/portfolios/{portfolioId}/summary")
    public ResponseEntity<SummaryResponse> summary(@PathVariable UUID portfolioId) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(portfolioService.summary(user, portfolioId));
    }

    @GetMapping("/portfolios/{portfolioId}/suggested-questions")
    public ResponseEntity<SuggestedQuestionsResponse> suggestedQuestions(@PathVariable UUID portfolioId) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(portfolioService.suggestedQuestions(user, portfolioId));
    }

    @PostMapping("/portfolios/{portfolioId}/save")
    public ResponseEntity<Void> save(@PathVariable UUID portfolioId) {
        User user = currentUserService.getCurrentUser();
        portfolioService.savePortfolio(user, portfolioId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/portfolios/{portfolioId}/save")
    public ResponseEntity<Void> unsave(@PathVariable UUID portfolioId) {
        User user = currentUserService.getCurrentUser();
        portfolioService.unsavePortfolio(user, portfolioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/me/saved-portfolios")
    public ResponseEntity<SavedPortfolioListResponse> savedPortfolios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(portfolioService.savedPortfolios(user, page, size));
    }
}
