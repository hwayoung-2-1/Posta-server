package com.hwayoung.hwayoungserver.portfolio.application;

import com.hwayoung.hwayoungserver.common.ApiException;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PageOwnerNote;
import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioFile;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioIndexJob;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioPage;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioSummary;
import com.hwayoung.hwayoungserver.portfolio.domain.model.SavedPortfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.model.SuggestedQuestion;
import com.hwayoung.hwayoungserver.portfolio.domain.type.JobStatus;
import com.hwayoung.hwayoungserver.portfolio.domain.type.JobType;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioStatus;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioVisibility;
import com.hwayoung.hwayoungserver.portfolio.domain.type.SummaryType;
import com.hwayoung.hwayoungserver.portfolio.exception.FileUploadFailedException;
import com.hwayoung.hwayoungserver.portfolio.persistence.PageOwnerNoteRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioFileRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioIndexJobRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioPageRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioSummaryRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.SavedPortfolioRepository;
import com.hwayoung.hwayoungserver.portfolio.persistence.SuggestedQuestionRepository;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.OwnerNoteResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PageDetailResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PageListItemResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PageListResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PortfolioDetailResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PortfolioListItemResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PortfolioListResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.ProcessingStatusResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.ProcessingStepResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.PublishPortfolioResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.ReindexResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.SavedPortfolioListItemResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.SavedPortfolioListResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.SuggestedQuestionItemResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.SuggestedQuestionsResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.SummaryResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.request.UpdatePortfolioRequest;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.UpdatePortfolioResponse;
import com.hwayoung.hwayoungserver.portfolio.presentation.dto.response.UploadPortfolioResponse;
import com.hwayoung.hwayoungserver.taxonomy.domain.model.RoleEntity;
import com.hwayoung.hwayoungserver.taxonomy.persistence.RoleRepository;
import com.hwayoung.hwayoungserver.taxonomy.domain.model.SkillEntity;
import com.hwayoung.hwayoungserver.taxonomy.persistence.SkillRepository;
import com.hwayoung.hwayoungserver.storage.FileStorageService;
import com.hwayoung.hwayoungserver.storage.StoredFile;
import com.hwayoung.hwayoungserver.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final PortfolioFileRepository portfolioFileRepository;
    private final PortfolioPageRepository portfolioPageRepository;
    private final PageOwnerNoteRepository pageOwnerNoteRepository;
    private final PortfolioSummaryRepository portfolioSummaryRepository;
    private final SuggestedQuestionRepository suggestedQuestionRepository;
    private final PortfolioIndexJobRepository portfolioIndexJobRepository;
    private final SavedPortfolioRepository savedPortfolioRepository;
    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final FileStorageService fileStorageService;
    private final PortfolioVectorIndexService portfolioVectorIndexService;
    private final PdfPageCountReader pdfPageCountReader;
    private final PdfFirstPageThumbnailRenderer pdfFirstPageThumbnailRenderer;
    private final PortfolioPdfProperties portfolioPdfProperties;

    @Transactional
    public UploadPortfolioResponse upload(
            User owner,
            MultipartFile file,
            String title,
            String description,
            PortfolioVisibility visibility,
            List<UUID> roleIds,
            List<UUID> skillIds
    ) {
        validateUpload(file, title);
        byte[] bytes = readBytes(file);
        int pageCount = pdfPageCountReader.countPages(bytes);
        byte[] thumbnailBytes = pdfFirstPageThumbnailRenderer.render(bytes);

        Portfolio portfolio = new Portfolio(owner, title, description, visibility);
        portfolio.replaceRoles(resolveRoles(roleIds));
        portfolio.replaceSkills(resolveSkills(skillIds));
        portfolioRepository.save(portfolio);

        String thumbnailObjectKey = thumbnailObjectKey(owner, portfolio);
        StoredFile storedFile = null;
        boolean uploadedThumbnail = false;
        boolean uploadedPortfolioFile = false;
        try {
            fileStorageService.upload(thumbnailObjectKey, thumbnailBytes, PdfFirstPageThumbnailRenderer.CONTENT_TYPE);
            uploadedThumbnail = true;
            deleteUploadedObjectOnRollback(thumbnailObjectKey);

            storedFile = fileStorageService.storePortfolioFile(portfolio.getId(), file);
            uploadedPortfolioFile = true;
            deleteUploadedObjectOnRollback(storedFile.objectName());
            portfolio.updateThumbnailObjectKey(thumbnailObjectKey);
            portfolio.updatePdfMetadata(
                    storedFile.objectName(),
                    safeOriginalFilename(file),
                    contentType(file),
                    file.getSize(),
                    pageCount
            );
            PortfolioFile portfolioFile = portfolioFileRepository.save(new PortfolioFile(
                    portfolio,
                    safeOriginalFilename(file),
                    storedFile.storageUrl(),
                    contentType(file),
                    file.getSize()
            ));
            portfolioPageRepository.save(new PortfolioPage(portfolio, portfolioFile, 1, null, ""));
        } catch (RuntimeException exception) {
            if (uploadedPortfolioFile && storedFile != null) {
                fileStorageService.delete(storedFile.objectName());
            }
            if (uploadedThumbnail) {
                fileStorageService.delete(thumbnailObjectKey);
            }
            throw exception;
        }
        portfolioSummaryRepository.save(new PortfolioSummary(portfolio, SummaryType.SHORT, defaultSummary(portfolio)));
        suggestedQuestionRepository.saveAll(List.of(
                new SuggestedQuestion(portfolio, null, "이 포트폴리오에서 가장 핵심 프로젝트는 무엇인가요?", "DEFAULT", 0),
                new SuggestedQuestion(portfolio, null, "작성자가 맡은 역할과 기여도는 무엇인가요?", "DEFAULT", 1),
                new SuggestedQuestion(portfolio, null, "사용된 기술과 도구는 무엇인가요?", "DEFAULT", 2)
        ));

        return UploadPortfolioResponse.of(portfolio.getId(), portfolio.getStatus(), storedFile.accessUrl());
    }

    @Transactional(readOnly = true)
    public PortfolioListResponse list(User viewer, int page, int size, String role, String skill, String name, String keyword) {
        Pageable pageable = pageable(page, size);
        List<Portfolio> filtered = portfolioRepository.findByStatusNot(PortfolioStatus.DELETED).stream()
                .filter(portfolio -> canList(portfolio, viewer))
                .filter(portfolio -> matchesRole(portfolio, role))
                .filter(portfolio -> matchesSkill(portfolio, skill))
                .filter(portfolio -> containsIgnoreCase(portfolio.getOwner().getName(), name))
                .filter(portfolio -> matchesKeyword(portfolio, keyword))
                .sorted(Comparator.comparing(Portfolio::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        int fromIndex = Math.min((int) pageable.getOffset(), filtered.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), filtered.size());
        List<PortfolioListItemResponse> content = filtered.subList(fromIndex, toIndex).stream()
                .map(portfolio -> PortfolioListItemResponse.of(
                        portfolio,
                        thumbnailUrl(portfolio),
                        viewer != null && savedPortfolioRepository.existsByUserAndPortfolio(viewer, portfolio)
                ))
                .toList();
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / pageable.getPageSize());
        return new PortfolioListResponse(content, pageable.getPageNumber(), pageable.getPageSize(), filtered.size(), totalPages);
    }

    @Transactional(readOnly = true)
    public PortfolioDetailResponse detail(User viewer, UUID portfolioId) {
        Portfolio portfolio = getAccessiblePortfolio(viewer, portfolioId);
        int pageCount = portfolioFileRepository.findByPortfolio(portfolio)
                .map(PortfolioFile::getPageCount)
                .orElse(0);
        return PortfolioDetailResponse.of(portfolio, pageCount, summaryContent(portfolio));
    }

    @Transactional
    public UpdatePortfolioResponse update(User owner, UUID portfolioId, UpdatePortfolioRequest request) {
        Portfolio portfolio = getOwnedPortfolio(owner, portfolioId);
        portfolio.update(request.title(), request.description(), request.visibility());
        if (request.roleIds() != null) {
            portfolio.replaceRoles(resolveRoles(request.roleIds()));
        }
        if (request.skillIds() != null) {
            portfolio.replaceSkills(resolveSkills(request.skillIds()));
        }
        return UpdatePortfolioResponse.from(portfolio);
    }

    @Transactional
    public void delete(User owner, UUID portfolioId) {
        Portfolio portfolio = getOwnedPortfolio(owner, portfolioId);
        portfolio.markDeleted();
    }

    @Transactional
    public PublishPortfolioResponse publish(User owner, UUID portfolioId) {
        Portfolio portfolio = getOwnedPortfolio(owner, portfolioId);
        if (portfolio.getStatus() == PortfolioStatus.PROCESSING) {
            throw ApiException.conflict("PORTFOLIO_NOT_READY", "처리 완료 전에는 게시할 수 없습니다.");
        }
        portfolio.publish(generateSlug(portfolio.getTitle()));
        return PublishPortfolioResponse.from(portfolio);
    }

    @Transactional(readOnly = true)
    public ProcessingStatusResponse processingStatus(User viewer, UUID portfolioId) {
        Portfolio portfolio = getAccessiblePortfolio(viewer, portfolioId);
        PortfolioFile file = portfolioFileRepository.findByPortfolio(portfolio).orElse(null);
        String status = file == null ? "PENDING" : file.getProcessingStatus().name();
        String failureReason = file == null ? null : file.getFailureReason();
        return new ProcessingStatusResponse(
                portfolio.getId(),
                portfolio.getStatus(),
                List.of(
                        new ProcessingStepResponse("PDF_RENDER", status),
                        new ProcessingStepResponse("TEXT_EXTRACTION", status),
                        new ProcessingStepResponse("RAG_INDEX", "DONE")
                ),
                failureReason
        );
    }

    @Transactional(readOnly = true)
    public PageListResponse pages(User viewer, UUID portfolioId) {
        Portfolio portfolio = getAccessiblePortfolio(viewer, portfolioId);
        List<PageListItemResponse> pages = portfolioPageRepository.findByPortfolioOrderByPageNumberAsc(portfolio).stream()
                .map(page -> PageListItemResponse.of(page, pageOwnerNoteRepository.findByPortfolioPage(page).isPresent()))
                .toList();
        return new PageListResponse(portfolio.getId(), pages);
    }

    @Transactional(readOnly = true)
    public PageDetailResponse pageDetail(User viewer, UUID portfolioId, int pageNumber) {
        Portfolio portfolio = getAccessiblePortfolio(viewer, portfolioId);
        PortfolioPage page = getPage(portfolio, pageNumber);
        String ownerNote = pageOwnerNoteRepository.findByPortfolioPage(page)
                .map(PageOwnerNote::getContent)
                .orElse(null);
        return PageDetailResponse.of(page, ownerNote);
    }

    @Transactional
    public OwnerNoteResponse saveOwnerNote(User owner, UUID portfolioId, int pageNumber, String content) {
        Portfolio portfolio = getOwnedPortfolio(owner, portfolioId);
        PortfolioPage page = getPage(portfolio, pageNumber);
        PageOwnerNote note = pageOwnerNoteRepository.findByPortfolioPage(page)
                .orElseGet(() -> pageOwnerNoteRepository.save(new PageOwnerNote(page, content)));
        note.update(content);
        portfolioIndexJobRepository.save(new PortfolioIndexJob(portfolio, JobType.REINDEX, JobStatus.PENDING));
        return OwnerNoteResponse.of(page, note.getContent());
    }

    @Transactional
    public ReindexResponse reindex(User owner, UUID portfolioId) {
        Portfolio portfolio = getOwnedPortfolio(owner, portfolioId);
        PortfolioIndexJob job = portfolioIndexJobRepository.save(new PortfolioIndexJob(portfolio, JobType.REINDEX, JobStatus.PENDING));
        try {
            job.markRunning();
            portfolioVectorIndexService.reindex(portfolio);
            job.markDone();
        } catch (RuntimeException exception) {
            job.markFailed(exception.getMessage());
            throw exception;
        }
        return new ReindexResponse(portfolio.getId(), job.getId(), job.getStatus());
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary(User viewer, UUID portfolioId) {
        Portfolio portfolio = getAccessiblePortfolio(viewer, portfolioId);
        return new SummaryResponse(portfolio.getId(), SummaryType.SHORT, summaryContent(portfolio));
    }

    @Transactional(readOnly = true)
    public SuggestedQuestionsResponse suggestedQuestions(User viewer, UUID portfolioId) {
        Portfolio portfolio = getAccessiblePortfolio(viewer, portfolioId);
        return new SuggestedQuestionsResponse(
                suggestedQuestionRepository.findByPortfolioOrderByDisplayOrderAsc(portfolio).stream()
                        .map(SuggestedQuestionItemResponse::from)
                        .toList()
        );
    }

    @Transactional
    public void savePortfolio(User user, UUID portfolioId) {
        Portfolio portfolio = getAccessiblePortfolio(user, portfolioId);
        if (!savedPortfolioRepository.existsByUserAndPortfolio(user, portfolio)) {
            savedPortfolioRepository.save(new SavedPortfolio(user, portfolio));
        }
    }

    @Transactional
    public void unsavePortfolio(User user, UUID portfolioId) {
        Portfolio portfolio = getAccessiblePortfolio(user, portfolioId);
        savedPortfolioRepository.deleteByUserAndPortfolio(user, portfolio);
    }

    @Transactional(readOnly = true)
    public SavedPortfolioListResponse savedPortfolios(User user, int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<SavedPortfolio> savedPortfolios = savedPortfolioRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return new SavedPortfolioListResponse(
                savedPortfolios.getContent().stream()
                        .map(SavedPortfolioListItemResponse::from)
                        .toList(),
                savedPortfolios.getNumber(),
                savedPortfolios.getSize(),
                savedPortfolios.getTotalElements()
        );
    }

    private Portfolio getAccessiblePortfolio(User viewer, UUID portfolioId) {
        Portfolio portfolio = portfolioRepository.findWithOwnerAndTagsById(portfolioId)
                .orElseThrow(() -> ApiException.notFound("PORTFOLIO_NOT_FOUND", "포트폴리오를 찾을 수 없습니다."));
        if (portfolio.getStatus() == PortfolioStatus.DELETED) {
            throw ApiException.notFound("PORTFOLIO_NOT_FOUND", "포트폴리오를 찾을 수 없습니다.");
        }
        if (portfolio.getOwner().getId().equals(viewer.getId())) {
            return portfolio;
        }
        if (portfolio.getStatus() == PortfolioStatus.PUBLISHED && portfolio.getVisibility() != PortfolioVisibility.PRIVATE) {
            return portfolio;
        }
        throw ApiException.forbidden("포트폴리오 접근 권한이 없습니다.");
    }

    private Portfolio getOwnedPortfolio(User owner, UUID portfolioId) {
        Portfolio portfolio = portfolioRepository.findWithOwnerAndTagsById(portfolioId)
                .orElseThrow(() -> ApiException.notFound("PORTFOLIO_NOT_FOUND", "포트폴리오를 찾을 수 없습니다."));
        if (portfolio.getStatus() == PortfolioStatus.DELETED) {
            throw ApiException.notFound("PORTFOLIO_NOT_FOUND", "포트폴리오를 찾을 수 없습니다.");
        }
        if (!portfolio.getOwner().getId().equals(owner.getId())) {
            throw ApiException.forbidden("포트폴리오 소유자만 요청할 수 있습니다.");
        }
        return portfolio;
    }

    private PortfolioPage getPage(Portfolio portfolio, int pageNumber) {
        return portfolioPageRepository.findByPortfolioAndPageNumber(portfolio, pageNumber)
                .orElseThrow(() -> ApiException.notFound("PORTFOLIO_NOT_FOUND", "포트폴리오 페이지를 찾을 수 없습니다."));
    }

    private void validateUpload(MultipartFile file, String title) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("PDF 파일은 필수입니다.");
        }
        if (title == null || title.isBlank()) {
            throw ApiException.badRequest("포트폴리오 제목은 필수입니다.");
        }
        String filename = safeOriginalFilename(file).toLowerCase(Locale.ROOT);
        if (!"application/pdf".equalsIgnoreCase(file.getContentType()) && !filename.endsWith(".pdf")) {
            throw ApiException.unprocessable("UNSUPPORTED_FILE_TYPE", "PDF 파일만 업로드할 수 있습니다.");
        }
    }

    private String safeOriginalFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return "portfolio.pdf";
        }
        return StringUtils.cleanPath(originalFilename).replaceAll("[\\\\/]", "_");
    }

    private String contentType(MultipartFile file) {
        return file.getContentType() == null ? "application/pdf" : file.getContentType();
    }

    private List<RoleEntity> resolveRoles(List<UUID> roleIds) {
        if (roleIds == null) {
            return List.of();
        }
        List<UUID> distinctIds = roleIds.stream().distinct().toList();
        List<RoleEntity> roles = roleRepository.findAllById(distinctIds);
        if (roles.size() != distinctIds.size()) {
            throw ApiException.badRequest("존재하지 않는 직군이 포함되어 있습니다.");
        }
        return roles;
    }

    private List<SkillEntity> resolveSkills(List<UUID> skillIds) {
        if (skillIds == null) {
            return List.of();
        }
        List<UUID> distinctIds = skillIds.stream().distinct().toList();
        List<SkillEntity> skills = skillRepository.findAllById(distinctIds);
        if (skills.size() != distinctIds.size()) {
            throw ApiException.badRequest("존재하지 않는 기술이 포함되어 있습니다.");
        }
        return skills;
    }

    private Pageable pageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 12 : Math.min(size, 100);
        return PageRequest.of(safePage, safeSize);
    }

    private boolean canList(Portfolio portfolio, User viewer) {
        if (viewer != null && portfolio.getOwner().getId().equals(viewer.getId())) {
            return true;
        }
        return isPubliclyListable(portfolio);
    }

    private boolean isPubliclyListable(Portfolio portfolio) {
        return portfolio.getVisibility() == PortfolioVisibility.PUBLIC
                && (portfolio.getStatus() == PortfolioStatus.READY || portfolio.getStatus() == PortfolioStatus.PUBLISHED);
    }

    private String thumbnailUrl(Portfolio portfolio) {
        if (StringUtils.hasText(portfolio.getThumbnailObjectKey())) {
            return fileStorageService.presignedGetUrl(
                    portfolio.getThumbnailObjectKey(),
                    portfolioPdfProperties.getViewUrlExpirySeconds()
            );
        }
        return portfolio.getThumbnailUrl();
    }

    private String thumbnailObjectKey(User owner, Portfolio portfolio) {
        return "portfolios/" + owner.getId() + "/" + portfolio.getId() + "/first-page.png";
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new FileUploadFailedException();
        }
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

    private boolean matchesRole(Portfolio portfolio, String role) {
        if (role == null || role.isBlank()) {
            return true;
        }
        return portfolio.getRoles().stream().anyMatch(item -> containsIgnoreCase(item.getName(), role));
    }

    private boolean matchesSkill(Portfolio portfolio, String skill) {
        if (skill == null || skill.isBlank()) {
            return true;
        }
        return portfolio.getSkills().stream().anyMatch(item -> containsIgnoreCase(item.getName(), skill));
    }

    private boolean matchesKeyword(Portfolio portfolio, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        return containsIgnoreCase(portfolio.getTitle(), keyword) || containsIgnoreCase(portfolio.getDescription(), keyword);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        if (value == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private String summaryContent(Portfolio portfolio) {
        return portfolioSummaryRepository.findFirstByPortfolioAndSummaryType(portfolio, SummaryType.SHORT)
                .map(PortfolioSummary::getContent)
                .orElseGet(() -> defaultSummary(portfolio));
    }

    private String defaultSummary(Portfolio portfolio) {
        if (portfolio.getDescription() != null && !portfolio.getDescription().isBlank()) {
            return portfolio.getDescription();
        }
        return portfolio.getTitle() + " 포트폴리오입니다.";
    }

    private String generateSlug(String title) {
        String base = Normalizer.normalize(title == null ? "portfolio" : title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "portfolio";
        }

        String slug;
        do {
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 6);
        } while (portfolioRepository.existsByPublicSlug(slug));
        return slug;
    }
}
