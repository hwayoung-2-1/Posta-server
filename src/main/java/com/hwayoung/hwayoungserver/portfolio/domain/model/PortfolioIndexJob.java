package com.hwayoung.hwayoungserver.portfolio.domain.model;

import com.hwayoung.hwayoungserver.common.AuditableEntity;
import com.hwayoung.hwayoungserver.portfolio.domain.type.JobStatus;
import com.hwayoung.hwayoungserver.portfolio.domain.type.JobType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "portfolio_index_jobs")
public class PortfolioIndexJob extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    protected PortfolioIndexJob() {
    }

    public PortfolioIndexJob(Portfolio portfolio, JobType jobType, JobStatus status) {
        this.portfolio = portfolio;
        this.jobType = jobType;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void markRunning() {
        this.status = JobStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void markDone() {
        this.status = JobStatus.DONE;
        this.finishedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.finishedAt = LocalDateTime.now();
    }
}
