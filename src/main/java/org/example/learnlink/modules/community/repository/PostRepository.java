package org.example.learnlink.modules.community.repository;

import org.example.learnlink.modules.community.dto.PostResponse;
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
import java.util.Optional;

/**
 * Repository for Post entity
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Find all posts by user ID
     */
    Page<Post> findByUserId(Long userId, Pageable pageable);

    Page<Post> findByUserIdAndHiddenIsFalse(Long userId, Pageable pageable);

    Optional<Post> findByIdAndHiddenIsFalse(Long postId);
    /**
     * Find all posts by category
     */
    Page<Post> findByCategoryAndHiddenIsFalse(PostCategory category, Pageable pageable);

    /**
     * Find all posts by type
     */
    Page<Post> findByTypeAndHiddenIsFalse(PostType type, Pageable pageable);

    /**
     * Find posts by category and type
     */
    Page<Post> findByCategoryAndTypeAndHiddenIsFalse(PostCategory category, PostType type, Pageable pageable);

    /**
     * Find popular posts ordered by view count
     */
    @Query("SELECT p FROM Post p WHERE p.hidden = false ORDER BY p.viewCount DESC")
    Page<Post> findPopularPostsAndHiddenIsFalse(Pageable pageable);

    /**
     * Find trending posts (most liked in the last 24 hours)
     */
    @Query("SELECT p FROM Post p WHERE p.hidden = false AND p.createdAt >= :since ORDER BY p.likesCount DESC")
    Page<Post> findTrendingPostsAndHiddenIsFalse(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Search posts by keyword in title or content
     */
    @Query("SELECT p FROM Post p WHERE p.hidden = false AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Search posts with all filters
     */
    @Query("SELECT p FROM Post p WHERE " +
           "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:type IS NULL OR p.type = :type)"+
            "AND p.hidden = false " )
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

    /**
     * Count posts by user
     */
    long countByUserId(Long userId);

    /**
     * Find recent posts by user ordered by creation date
     */
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Sum total likes on all posts by user
     */
    @Query("SELECT COALESCE(SUM(p.likesCount), 0) FROM Post p WHERE p.userId = :userId")
    int sumLikesCountByUserId(@Param("userId") Long userId);
}

