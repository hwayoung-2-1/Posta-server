package com.hwayoung.hwayoungserver.portfolio.application;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioStatus;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioVisibility;
import com.hwayoung.hwayoungserver.portfolio.persistence.PageOwnerNoteRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioFileRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioIndexJobRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioPageRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioSummaryRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.SavedPortfolioRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.SuggestedQuestionRepository;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PortfolioListResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.UploadPortfolioResponse;
import com.hwayoung.hwayoungserver.storage.FileStorageService;
import com.hwayoung.hwayoungserver.storage.StoredFile;
import com.hwayoung.hwayoungserver.taxonomy.persistence.RoleRepository;
import com.hwayoung.hwayoungserver.taxonomy.persistence.SkillRepository;
import com.hwayoung.hwayoungserver.user.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {
    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private PortfolioFileRepository portfolioFileRepository;
    @Mock
    private PortfolioPageRepository portfolioPageRepository;
    @Mock
    private PageOwnerNoteRepository pageOwnerNoteRepository;
    @Mock
    private PortfolioSummaryRepository portfolioSummaryRepository;
    @Mock
    private SuggestedQuestionRepository suggestedQuestionRepository;
    @Mock
    private PortfolioIndexJobRepository portfolioIndexJobRepository;
    @Mock
    private SavedPortfolioRepository savedPortfolioRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private PortfolioVectorIndexService portfolioVectorIndexService;

    private PortfolioService portfolioService;
    private PortfolioPdfProperties pdfProperties;
    private User viewer;
    private User other;

    @BeforeEach
    void setUp() {
        pdfProperties = new PortfolioPdfProperties();
        pdfProperties.setViewUrlExpirySeconds(600);
        portfolioService = new PortfolioService(
                portfolioRepository,
                portfolioFileRepository,
                portfolioPageRepository,
                pageOwnerNoteRepository,
                portfolioSummaryRepository,
                suggestedQuestionRepository,
                portfolioIndexJobRepository,
                savedPortfolioRepository,
                roleRepository,
                skillRepository,
                fileStorageService,
                portfolioVectorIndexService,
                new PdfPageCountReader(),
                new PdfFirstPageThumbnailRenderer(),
                pdfProperties
        );
        viewer = user("viewer@example.com");
        other = user("other@example.com");
    }

    @Test
    @DisplayName("전체 조회는 요청자 포트폴리오와 다른 사용자의 공개 포트폴리오를 반환한다")
    void listReturnsOwnPortfoliosAndOtherPublicPortfolio() {
        Portfolio ownPrivate = portfolio(viewer, "내 비공개 포트폴리오", PortfolioVisibility.PRIVATE, PortfolioStatus.READY);
        Portfolio ownPublic = portfolio(viewer, "내 공개 포트폴리오", PortfolioVisibility.PUBLIC, PortfolioStatus.READY);
        Portfolio otherPublicReady = portfolio(other, "다른 사용자 공개 포트폴리오", PortfolioVisibility.PUBLIC, PortfolioStatus.READY);
        otherPublicReady.updateThumbnailObjectKey("portfolios/other/public/first-page.png");
        Portfolio otherPrivate = portfolio(other, "다른 사용자 비공개 포트폴리오", PortfolioVisibility.PRIVATE, PortfolioStatus.READY);
        when(portfolioRepository.findByStatusNot(PortfolioStatus.DELETED))
                .thenReturn(List.of(ownPrivate, ownPublic, otherPublicReady, otherPrivate));
        when(fileStorageService.presignedGetUrl(eq(otherPublicReady.getThumbnailObjectKey()), eq(600)))
                .thenReturn("https://minio.example.com/first-page.png");

        PortfolioListResponse response = portfolioService.list(viewer, 0, 12, null, null, null, null);

        assertThat(response.content())
                .extracting(item -> item.title())
                .containsExactly("내 비공개 포트폴리오", "내 공개 포트폴리오", "다른 사용자 공개 포트폴리오");
        assertThat(response.content())
                .filteredOn(item -> item.title().equals("다른 사용자 공개 포트폴리오"))
                .singleElement()
                .extracting(item -> item.thumbnailUrl())
                .isEqualTo("https://minio.example.com/first-page.png");
    }

    @Test
    @DisplayName("포트폴리오 업로드 시 첫 페이지 썸네일을 생성해 MinIO에 저장한다")
    void uploadStoresFirstPageThumbnail() {
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> {
            Portfolio portfolio = invocation.getArgument(0);
            ReflectionTestUtils.setField(portfolio, "id", UUID.randomUUID());
            return portfolio;
        });
        when(fileStorageService.storePortfolioFile(any(UUID.class), any(MultipartFile.class)))
                .thenReturn(new StoredFile("portfolios/original.pdf", "minio://original.pdf", "https://minio.example.com/original.pdf"));
        MultipartFile file = pdfFile("portfolio.pdf");

        UploadPortfolioResponse response = portfolioService.upload(
                viewer,
                file,
                "백엔드 포트폴리오",
                "description",
                PortfolioVisibility.PUBLIC,
                null,
                null
        );

        ArgumentCaptor<String> objectKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<String> contentTypeCaptor = ArgumentCaptor.forClass(String.class);
        verify(fileStorageService).upload(objectKeyCaptor.capture(), bytesCaptor.capture(), contentTypeCaptor.capture());

        assertThat(response.fileUrl()).isEqualTo("https://minio.example.com/original.pdf");
        assertThat(objectKeyCaptor.getValue()).endsWith("/first-page.png");
        assertThat(bytesCaptor.getValue()).isNotEmpty();
        assertThat(contentTypeCaptor.getValue()).isEqualTo(PdfFirstPageThumbnailRenderer.CONTENT_TYPE);
    }

    @Test
    @DisplayName("비로그인 전체 조회는 다른 사용자의 공개 포트폴리오만 반환한다")
    void anonymousListReturnsOnlyPublicPortfolios() {
        Portfolio publicReady = portfolio(other, "공개 포트폴리오", PortfolioVisibility.PUBLIC, PortfolioStatus.READY);
        Portfolio privateReady = portfolio(other, "비공개 포트폴리오", PortfolioVisibility.PRIVATE, PortfolioStatus.READY);
        when(portfolioRepository.findByStatusNot(PortfolioStatus.DELETED))
                .thenReturn(List.of(publicReady, privateReady));

        PortfolioListResponse response = portfolioService.list(null, 0, 12, null, null, null, null);

        assertThat(response.content())
                .extracting(item -> item.title())
                .containsExactly("공개 포트폴리오");
    }

    private Portfolio portfolio(User owner, String title, PortfolioVisibility visibility, PortfolioStatus status) {
        Portfolio portfolio = new Portfolio(owner, title, "description", visibility);
        ReflectionTestUtils.setField(portfolio, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(portfolio, "status", status);
        return portfolio;
    }

    private User user(String email) {
        User user = new User(email, "password", email);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private MultipartFile pdfFile(String filename) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(outputStream);
            return new MockMultipartFile("file", filename, "application/pdf", outputStream.toByteArray());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
