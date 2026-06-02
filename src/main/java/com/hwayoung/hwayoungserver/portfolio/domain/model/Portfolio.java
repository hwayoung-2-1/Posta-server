package com.hwayoung.hwayoungserver.portfolio.domain.model;

import com.hwayoung.hwayoungserver.common.AuditableEntity;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioStatus;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioVisibility;
import com.hwayoung.hwayoungserver.taxonomy.domain.model.RoleEntity;
import com.hwayoung.hwayoungserver.taxonomy.domain.model.SkillEntity;
import com.hwayoung.hwayoungserver.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "portfolios")
public class Portfolio extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

     @Column(name = "page_count", nullable = false)
    private int pageCount = 0;

    @Column(name = "pdf_object_key", length = 512)
    private String pdfObjectKey;

    @Column(name = "pdf_original_filename")
    private String pdfOriginalFilename;

    @Column(name = "pdf_content_type", length = 100)
    private String pdfContentType;

    @Column(name = "pdf_size", nullable = false)
    private long pdfSize = 0L;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PortfolioVisibility visibility = PortfolioVisibility.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PortfolioStatus status = PortfolioStatus.READY;

    @Column(name = "public_slug", unique = true)
    private String publicSlug;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "thumbnail_object_key", length = 512)
    private String thumbnailObjectKey;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToMany
    @JoinTable(
            name = "portfolio_roles",
            joinColumns = @JoinColumn(name = "portfolio_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "portfolio_skills",
            joinColumns = @JoinColumn(name = "portfolio_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<SkillEntity> skills = new LinkedHashSet<>();

    protected Portfolio() {
    }

    public Portfolio(User owner, String title, String description, PortfolioVisibility visibility) {
        this.owner = owner;
        this.title = title;
        this.description = description;
        if (visibility != null) {
            this.visibility = visibility;
        }
    }

    public UUID getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPageCount() {
        return pageCount;
    }

    public String getPdfObjectKey() {
        return pdfObjectKey;
    }

    public String getPdfOriginalFilename() {
        return pdfOriginalFilename;
    }

    public String getPdfContentType() {
        return pdfContentType;
    }

    public long getPdfSize() {
        return pdfSize;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public PortfolioVisibility getVisibility() {
        return visibility;
    }

    public PortfolioStatus getStatus() {
        return status;
    }

    public String getPublicSlug() {
        return publicSlug;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getThumbnailObjectKey() {
        return thumbnailObjectKey;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public Set<SkillEntity> getSkills() {
        return skills;
    }

    public void replaceRoles(Collection<RoleEntity> roles) {
        this.roles.clear();
        if (roles != null) {
            this.roles.addAll(roles);
        }
    }

    public void replaceSkills(Collection<SkillEntity> skills) {
        this.skills.clear();
        if (skills != null) {
            this.skills.addAll(skills);
        }
    }

    public void update(String title, String description, PortfolioVisibility visibility) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (visibility != null) {
            this.visibility = visibility;
        }
    }

    public void updateThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void updateThumbnailObjectKey(String thumbnailObjectKey) {
        this.thumbnailObjectKey = thumbnailObjectKey;
    }

    public void updatePdfMetadata(
            String pdfObjectKey,
            String pdfOriginalFilename,
            String pdfContentType,
            long pdfSize,
            int pageCount
    ) {
        this.pdfObjectKey = pdfObjectKey;
        this.pdfOriginalFilename = pdfOriginalFilename;
        this.pdfContentType = pdfContentType;
        this.pdfSize = pdfSize;
        this.pageCount = pageCount;
    }

    public void publish(String publicSlug) {
        this.status = PortfolioStatus.PUBLISHED;
        this.publicSlug = publicSlug;
        this.publishedAt = LocalDateTime.now();
        if (this.visibility == PortfolioVisibility.PRIVATE) {
            this.visibility = PortfolioVisibility.PUBLIC;
        }
    }

    public void markDeleted() {
        this.status = PortfolioStatus.DELETED;
    }
}
