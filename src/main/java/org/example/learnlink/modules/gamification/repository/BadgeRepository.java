package org.example.learnlink.modules.gamification.repository;

import org.example.learnlink.modules.gamification.entity.Badge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Optional<Badge> findByCode(String code);

    List<Badge> findByActive(Boolean active);

    Page<Badge> findByRarity(String rarity, Pageable pageable);

    Page<Badge> findByType(String type, Pageable pageable);

    long countByActive(Boolean active);
}

