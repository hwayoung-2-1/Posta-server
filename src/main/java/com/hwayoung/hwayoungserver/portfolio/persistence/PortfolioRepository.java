package com.hwayoung.hwayoungserver.portfolio.persistence;

import com.hwayoung.hwayoungserver.portfolio.domain.model.Portfolio;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioStatus;
import com.hwayoung.hwayoungserver.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
    long countByOwnerAndStatusNot(User owner, PortfolioStatus status);

    @EntityGraph(attributePaths = {"owner", "roles", "skills"})
    List<Portfolio> findByStatusNot(PortfolioStatus status);

    @EntityGraph(attributePaths = {"owner", "roles", "skills"})
    @Query("select p from Portfolio p where p.id = :id")
    Optional<Portfolio> findWithOwnerAndTagsById(@Param("id") UUID id);

    boolean existsByPublicSlug(String publicSlug);
}
