package org.example.learnlink.modules.community.repository;

import org.example.learnlink.modules.community.entity.Post;
import org.example.learnlink.modules.community.entity.PostCategory;
import org.example.learnlink.modules.community.entity.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Post entity
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Find all posts by user ID
     */
    Page<Post> findByUserId(Long userId, Pageable pageable);

    /**
     * Find all posts by category
     */
    Page<Post> findByCategory(PostCategory category, Pageable pageable);

    /**
     * Find all posts by type
     */
    Page<Post> findByType(PostType type, Pageable pageable);

    /**
     * Find posts by category and type
     */
    Page<Post> findByCategoryAndType(PostCategory category, PostType type, Pageable pageable);

    /**
     * Find popular posts ordered by view count
     */
    @Query("SELECT p FROM Post p ORDER BY p.viewCount DESC")
    Page<Post> findPopularPosts(Pageable pageable);

    /**
     * Find trending posts (most liked in the last 24 hours)
     */
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :since ORDER BY p.likesCount DESC")
    Page<Post> findTrendingPosts(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Search posts by keyword in title or content
     */
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Search posts with all filters
     */
    @Query("SELECT p FROM Post p WHERE " +
           "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:type IS NULL OR p.type = :type)")
    Page<Post> searchWithFilters(
        @Param("keyword") String keyword,
        @Param("category") PostCategory category,
        @Param("type") PostType type,
        Pageable pageable
    );
    
    // Admin statistics queries
    
    /**
     * Count posts created after a specific date
     */
    long countByCreatedAtAfter(java.time.LocalDateTime since);

    // Moderation queries
    
    /**
     * Find all hidden posts
     */
    Page<Post> findByHiddenTrue(Pageable pageable);

    /**
     * Find all visible (not hidden) posts
     */
    Page<Post> findByHiddenFalse(Pageable pageable);
}

