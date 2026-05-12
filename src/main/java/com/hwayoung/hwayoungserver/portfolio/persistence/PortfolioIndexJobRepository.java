package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.PortfolioIndexJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PortfolioIndexJobRepository extends JpaRepository<PortfolioIndexJob, UUID> {
}
