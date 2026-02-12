package org.example.learnlink.modules.community.repository;

import org.example.learnlink.modules.community.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for PostLike entity
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * Find a like by post ID and user ID
     */
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * Check if a user has liked a post
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /**
     * Count likes on a post
     */
    long countByPostId(Long postId);

    /**
     * Delete a like
     */
    void deleteByPostIdAndUserId(Long postId, Long userId);
}

