package com.hwayoung.hwayoungserver.portfolio.application;

import com.hwayoung.hwayoungserver.common.ApiException;
import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioStatus;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioVisibility;
import com.hwayoung.hwayoungserver.portfolio.exception.FileSizeExceededException;
import com.hwayoung.hwayoungserver.portfolio.exception.FileUploadFailedException;
import com.hwayoung.hwayoungserver.portfolio.exception.InvalidPdfException;
import com.hwayoung.hwayoungserver.portfolio.exception.PortfolioAccessDeniedException;
import com.hwayoung.hwayoungserver.portfolio.exception.PortfolioNotFoundException;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioContextRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioRepository;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PdfViewUrlResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PortfolioDetailResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.UploadPortfolioPdfResponse;
import com.hwayoung.hwayoungserver.storage.FileStorageService;
import com.hwayoung.hwayoungserver.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioPdfService {
    private static final String DEFAULT_PDF_CONTENT_TYPE = "application/pdf";

    private final PortfolioRepository portfolioRepository;
    private final PortfolioContextRepository portfolioContextRepository;
    private final FileStorageService fileStorageService;
    private final PdfPageCountReader pdfPageCountReader;
    private final PdfFirstPageThumbnailRenderer pdfFirstPageThumbnailRenderer;
    private final PortfolioPdfProperties properties;

    @Transactional
    public UploadPortfolioPdfResponse uploadPdfPortfolio(
            User owner,
            MultipartFile file,
            String title,
            String description,
            String visibilityValue
    ) {
        validateUploadRequest(file, title, visibilityValue);
        byte[] bytes = readBytes(file);
        int pageCount = pdfPageCountReader.countPages(bytes);
        byte[] thumbnailBytes = pdfFirstPageThumbnailRenderer.render(bytes);
        String originalFilename = safeOriginalFilename(file);
        String contentType = safeContentType(file);
        PortfolioVisibility visibility = parseVisibility(visibilityValue);

        Portfolio portfolio = portfolioRepository.saveAndFlush(new Portfolio(owner, title, description, visibility));
        String objectKey = "portfolios/" + owner.getId() + "/" + portfolio.getId() + "/original.pdf";
        String thumbnailObjectKey = "portfolios/" + owner.getId() + "/" + portfolio.getId() + "/first-page.png";
        boolean uploaded = false;
        boolean thumbnailUploaded = false;

        try {
            fileStorageService.upload(thumbnailObjectKey, thumbnailBytes, PdfFirstPageThumbnailRenderer.CONTENT_TYPE);
            thumbnailUploaded = true;
            deleteUploadedObjectOnRollback(thumbnailObjectKey);
            fileStorageService.upload(objectKey, bytes, contentType);
            uploaded = true;
            deleteUploadedObjectOnRollback(objectKey);
            portfolio.updateThumbnailObjectKey(thumbnailObjectKey);
            portfolio.updatePdfMetadata(objectKey, originalFilename, contentType, file.getSize(), pageCount);
            return UploadPortfolioPdfResponse.from(portfolioRepository.saveAndFlush(portfolio));
        } catch (ApiException exception) {
            if (uploaded) {
                fileStorageService.delete(objectKey);
            }
            if (thumbnailUploaded) {
                fileStorageService.delete(thumbnailObjectKey);
            }
            throw exception;
        } catch (RuntimeException exception) {
            if (uploaded) {
                fileStorageService.delete(objectKey);
            }
            if (thumbnailUploaded) {
                fileStorageService.delete(thumbnailObjectKey);
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public PortfolioDetailResponse getPortfolio(User viewer, UUID portfolioId) {
        return PortfolioDetailResponse.from(getAccessiblePortfolio(viewer, portfolioId));
    }

    @Transactional(readOnly = true)
    public PdfViewUrlResponse getPdfViewUrl(User viewer, UUID portfolioId) {
        Portfolio portfolio = getAccessiblePortfolio(viewer, portfolioId);
        if (portfolio.getPdfObjectKey() == null || portfolio.getPdfObjectKey().isBlank()) {
            throw new PortfolioNotFoundException();
        }

        int expirySeconds = properties.getViewUrlExpirySeconds();
        String url = fileStorageService.presignedGetUrl(portfolio.getPdfObjectKey(), expirySeconds);
        return new PdfViewUrlResponse(portfolio.getId(), url, LocalDateTime.now().plusSeconds(expirySeconds));
    }

    @Transactional
    public void deletePortfolio(User owner, UUID portfolioId) {
        Portfolio portfolio = getOwnedPortfolio(owner, portfolioId);
        String objectKey = portfolio.getPdfObjectKey();
        String thumbnailObjectKey = portfolio.getThumbnailObjectKey();
        portfolioContextRepository.deleteByPortfolioId(portfolio.getId());
        portfolioRepository.delete(portfolio);
        deleteObjectAfterCommit(objectKey);
        deleteObjectAfterCommit(thumbnailObjectKey);
    }

    Portfolio getAccessiblePortfolio(User viewer, UUID portfolioId) {
        Portfolio portfolio = portfolioRepository.findWithOwnerById(portfolioId)
                .orElseThrow(PortfolioNotFoundException::new);
        if (portfolio.getStatus() == PortfolioStatus.DELETED) {
            throw new PortfolioNotFoundException();
        }
        if (portfolio.getVisibility().isPublic()) {
            return portfolio;
        }
        if (viewer != null && portfolio.getOwner().getId().equals(viewer.getId())) {
            return portfolio;
        }
        throw new PortfolioAccessDeniedException("private 포트폴리오는 작성자만 조회할 수 있습니다.");
    }

    Portfolio getOwnedPortfolio(User owner, UUID portfolioId) {
        Portfolio portfolio = portfolioRepository.findWithOwnerById(portfolioId)
                .orElseThrow(PortfolioNotFoundException::new);
        if (portfolio.getStatus() == PortfolioStatus.DELETED) {
            throw new PortfolioNotFoundException();
        }
        if (!portfolio.getOwner().getId().equals(owner.getId())) {
            throw new PortfolioAccessDeniedException("포트폴리오 작성자만 요청할 수 있습니다.");
        }
        return portfolio;
    }

    private void validateUploadRequest(MultipartFile file, String title, String visibilityValue) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPdfException("PDF 파일은 필수입니다.");
        }
        if (title == null || title.isBlank()) {
            throw ApiException.badRequest("포트폴리오 제목은 필수입니다.");
        }
        if (file.getSize() > properties.getMaxSizeBytes()) {
            throw new FileSizeExceededException(properties.getMaxSizeBytes());
        }
        if (!isPdf(file)) {
            throw new InvalidPdfException("PDF 파일만 업로드할 수 있습니다.");
        }
        parseVisibility(visibilityValue);
    }

    private PortfolioVisibility parseVisibility(String visibilityValue) {
        try {
            return PortfolioVisibility.fromPdfUploadValue(visibilityValue);
        } catch (IllegalArgumentException exception) {
            throw ApiException.badRequest("visibility 값은 public 또는 private만 사용할 수 있습니다.");
        }
    }

    private boolean isPdf(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = safeOriginalFilename(file).toLowerCase(Locale.ROOT);
        return DEFAULT_PDF_CONTENT_TYPE.equalsIgnoreCase(contentType) || filename.endsWith(".pdf");
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new FileUploadFailedException();
        }
    }

    private String safeOriginalFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return "portfolio.pdf";
        }
        return StringUtils.cleanPath(originalFilename).replaceAll("[\\\\/]", "_");
    }

    private String safeContentType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType == null || contentType.isBlank() ? DEFAULT_PDF_CONTENT_TYPE : contentType;
    }

    private void deleteObjectAfterCommit(String objectKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            fileStorageService.delete(objectKey);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                fileStorageService.delete(objectKey);
            }
        });
    }

    private void deleteUploadedObjectOnRollback(String objectKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    fileStorageService.delete(objectKey);
                }
            }
        });
    }
}
