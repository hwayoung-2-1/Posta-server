package com.hwayoung.hwayoungserver.portfolio.presentation;

import com.hwayoung.hwayoungserver.portfolio.application.PortfolioPdfService;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PdfViewUrlResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PortfolioDetailResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.UploadPortfolioPdfResponse;
import com.hwayoung.hwayoungserver.user.CurrentUserService;
import com.hwayoung.hwayoungserver.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
public class PortfolioPdfController {
    private final CurrentUserService currentUserService;
    private final PortfolioPdfService portfolioPdfService;

    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadPortfolioPdfResponse> uploadPdfPortfolio(
            @RequestPart("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String visibility
    ) {
        User user = currentUserService.getCurrentUser();
        UploadPortfolioPdfResponse response = portfolioPdfService.uploadPdfPortfolio(
                user,
                file,
                title,
                description,
                visibility
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioDetailResponse> getPortfolio(@PathVariable UUID portfolioId) {
        Optional<User> user = currentUserService.getCurrentUserOptional();
        return ResponseEntity.ok(portfolioPdfService.getPortfolio(user.orElse(null), portfolioId));
    }

    @GetMapping("/{portfolioId}/pdf/view-url")
    public ResponseEntity<PdfViewUrlResponse> getPdfViewUrl(@PathVariable UUID portfolioId) {
        Optional<User> user = currentUserService.getCurrentUserOptional();
        return ResponseEntity.ok(portfolioPdfService.getPdfViewUrl(user.orElse(null), portfolioId));
    }

    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable UUID portfolioId) {
        User user = currentUserService.getCurrentUser();
        portfolioPdfService.deletePortfolio(user, portfolioId);
        return ResponseEntity.noContent().build();
    }
}
