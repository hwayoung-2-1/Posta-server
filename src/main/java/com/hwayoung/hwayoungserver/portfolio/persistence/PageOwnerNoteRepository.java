package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.PageOwnerNote;
import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PageOwnerNoteRepository extends JpaRepository<PageOwnerNote, UUID> {
    Optional<PageOwnerNote> findByPortfolioPage(PortfolioPage portfolioPage);
}
