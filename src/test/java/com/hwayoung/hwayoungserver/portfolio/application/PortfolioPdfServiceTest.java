package com.hwayoung.hwayoungserver.portfolio.application;

import com.hwayoung.hwayoungserver.common.ApiException;
import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioVisibility;
import com.hwayoung.hwayoungserver.portfolio.exception.InvalidPdfException;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioContextRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioRepository;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PortfolioDetailResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.UploadPortfolioPdfResponse;
import com.hwayoung.hwayoungserver.storage.FileStorageService;
import com.hwayoung.hwayoungserver.storage.StoredFile;
import com.hwayoung.hwayoungserver.user.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioPdfServiceTest {
    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private PortfolioContextRepository portfolioContextRepository;

    private RecordingFileStorageService fileStorageService;
    private PortfolioPdfService portfolioPdfService;
    private User owner;
    private UUID ownerId;
    private UUID portfolioId;

    @BeforeEach
    void setUp() {
        fileStorageService = new RecordingFileStorageService();
        PortfolioPdfProperties properties = new PortfolioPdfProperties();
        properties.setMaxSizeBytes(50L * 1024L * 1024L);
        properties.setViewUrlExpirySeconds(600);
        portfolioPdfService = new PortfolioPdfService(
                portfolioRepository,
                portfolioContextRepository,
                fileStorageService,
                new PdfPageCountReader(),
                new PdfFirstPageThumbnailRenderer(),
                properties
        );

        ownerId = UUID.randomUUID();
        portfolioId = UUID.randomUUID();
        owner = user(ownerId, "owner@example.com");
    }

    @Test
    @DisplayName("PDF 업로드 성공 시 페이지 수와 PDF 메타데이터를 저장한다")
    void uploadPdfPortfolioSuccess() {
        when(portfolioRepository.saveAndFlush(any(Portfolio.class))).thenAnswer(invocation -> {
            Portfolio portfolio = invocation.getArgument(0);
            if (portfolio.getId() == null) {
                ReflectionTestUtils.setField(portfolio, "id", portfolioId);
            }
            return portfolio;
        });

        MultipartFile file = pdfFile("portfolio.pdf", 2);

        UploadPortfolioPdfResponse response = portfolioPdfService.uploadPdfPortfolio(
                owner,
                file,
                "백엔드 포트폴리오",
                "Spring Boot 프로젝트",
                "private"
        );

        assertThat(response.id()).isEqualTo(portfolioId);
        assertThat(response.pageCount()).isEqualTo(2);
        assertThat(response.pdf().originalFilename()).isEqualTo("portfolio.pdf");
        assertThat(response.pdf().contentType()).isEqualTo("application/pdf");
        assertThat(response.pdf().size()).isEqualTo(file.getSize());
        assertThat(fileStorageService.uploadedObjectKey)
                .isEqualTo("portfolios/" + ownerId + "/" + portfolioId + "/original.pdf");
        assertThat(fileStorageService.uploadedObjectKeys)
                .containsExactly(
                        "portfolios/" + ownerId + "/" + portfolioId + "/first-page.png",
                        "portfolios/" + ownerId + "/" + portfolioId + "/original.pdf"
                );
    }

    @Test
    @DisplayName("PDF가 아닌 파일 업로드는 400을 반환한다")
    void uploadNonPdfFails() {
        MultipartFile file = new MockMultipartFile("file", "portfolio.txt", "text/plain", "not pdf".getBytes());

        assertThatThrownBy(() -> portfolioPdfService.uploadPdfPortfolio(owner, file, "title", null, "private"))
                .isInstanceOf(InvalidPdfException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(portfolioRepository, never()).saveAndFlush(any());
        assertThat(fileStorageService.uploadedObjectKey).isNull();
    }

    @Test
    @DisplayName("손상된 PDF 업로드는 400을 반환한다")
    void uploadBrokenPdfFails() {
        MultipartFile file = new MockMultipartFile("file", "broken.pdf", "application/pdf", "broken".getBytes());

        assertThatThrownBy(() -> portfolioPdfService.uploadPdfPortfolio(owner, file, "title", null, "private"))
                .isInstanceOf(InvalidPdfException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(portfolioRepository, never()).saveAndFlush(any());
        assertThat(fileStorageService.uploadedObjectKey).isNull();
    }

    @Test
    @DisplayName("private 포트폴리오는 작성자가 아닌 사용자가 조회할 수 없다")
    void privatePortfolioAccessDenied() {
        Portfolio portfolio = portfolio(owner, PortfolioVisibility.PRIVATE, 3);
        when(portfolioRepository.findWithOwnerById(portfolioId)).thenReturn(Optional.of(portfolio));

        User other = user(UUID.randomUUID(), "other@example.com");

        assertThatThrownBy(() -> portfolioPdfService.getPortfolio(other, portfolioId))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("public 포트폴리오는 다른 사용자도 조회할 수 있다")
    void publicPortfolioAccessible() {
        Portfolio portfolio = portfolio(owner, PortfolioVisibility.PUBLIC, 3);
        when(portfolioRepository.findWithOwnerById(portfolioId)).thenReturn(Optional.of(portfolio));

        User other = user(UUID.randomUUID(), "other@example.com");

        PortfolioDetailResponse response = portfolioPdfService.getPortfolio(other, portfolioId);

        assertThat(response.id()).isEqualTo(portfolioId);
        assertThat(response.ownerId()).isEqualTo(ownerId);
        assertThat(response.visibility()).isEqualTo("public");
        assertThat(response.pageCount()).isEqualTo(3);
    }

    private Portfolio portfolio(User owner, PortfolioVisibility visibility, int pageCount) {
        Portfolio portfolio = new Portfolio(owner, "title", "description", visibility);
        ReflectionTestUtils.setField(portfolio, "id", portfolioId);
        portfolio.updatePdfMetadata(
                "portfolios/" + owner.getId() + "/" + portfolioId + "/original.pdf",
                "portfolio.pdf",
                "application/pdf",
                1024L,
                pageCount
        );
        return portfolio;
    }

    private User user(UUID id, String email) {
        User user = new User(email, "password", email);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private MultipartFile pdfFile(String filename, int pageCount) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (int i = 0; i < pageCount; i++) {
                document.addPage(new PDPage());
            }
            document.save(outputStream);
            return new MockMultipartFile("file", filename, "application/pdf", outputStream.toByteArray());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static class RecordingFileStorageService implements FileStorageService {
        private String uploadedObjectKey;
        private final List<String> uploadedObjectKeys = new ArrayList<>();

        @Override
        public StoredFile storePortfolioFile(UUID portfolioId, MultipartFile file) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void upload(String objectKey, byte[] bytes, String contentType) {
            this.uploadedObjectKey = objectKey;
            this.uploadedObjectKeys.add(objectKey);
        }

        @Override
        public void delete(String objectKey) {
        }

        @Override
        public String presignedGetUrl(String objectKey, int expirySeconds) {
            return "https://minio.example.com/" + objectKey;
        }

        @Override
        public String objectUrl(String objectKey) {
            return "https://minio.example.com/" + objectKey;
        }

        @Override
        public String viewUrl(String objectKey, int expirySeconds) {
            return "https://minio.example.com/" + objectKey;
        }
    }
}
