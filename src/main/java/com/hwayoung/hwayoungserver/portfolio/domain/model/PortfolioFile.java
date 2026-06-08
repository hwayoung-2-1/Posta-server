package com.hwayoung.hwayoungserver.portfolio.domain.model;

import com.hwayoung.hwayoungserver.common.AuditableEntity;
import com.hwayoung.hwayoungserver.portfolio.domain.type.ProcessingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "portfolio_files")
public class PortfolioFile extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false, unique = true)
    private Portfolio portfolio;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "original_file_url", nullable = false)
    private String originalFileUrl;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "page_count", nullable = false)
    private int pageCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private ProcessingStatus processingStatus;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    protected PortfolioFile() {
    }

    public PortfolioFile(
            Portfolio portfolio,
            String originalFileName,
            String originalFileUrl,
            String contentType,
            long sizeBytes
    ) {
        this.portfolio = portfolio;
        this.originalFileName = originalFileName;
        this.originalFileUrl = originalFileUrl;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.pageCount = 1;
        this.processingStatus = ProcessingStatus.DONE;
    }

    public UUID getId() {
        return id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getOriginalFileUrl() {
        return originalFileUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public int getPageCount() {
        return pageCount;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
