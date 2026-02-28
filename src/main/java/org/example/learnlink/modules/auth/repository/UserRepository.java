package org.example.learnlink.modules.auth.repository;

import org.example.learnlink.modules.auth.entity.User;
import org.example.learnlink.modules.auth.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationCode(@Param("code") String code);
    Optional<User> findByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    // Admin statistics queries
    
    /**
     * Count users created after a specific date
     */
    long countByCreatedAtAfter(LocalDateTime since);
    
    /**
     * Find all users with filters for admin
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:active IS NULL OR u.active = :active) AND " +
           "(:search IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findAllWithFilters(
        @Param("role") UserRole role,
        @Param("active") Boolean active,
        @Param("search") String search,
        Pageable pageable
    );
}
