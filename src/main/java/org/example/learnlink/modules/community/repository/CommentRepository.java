package org.example.learnlink.modules.community.repository;

import org.example.learnlink.modules.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Comment entity
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Find all comments for a post
     */
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    /**
     * Find all comments for an answer
     */
    List<Comment> findByAnswerIdOrderByCreatedAtDesc(Long answerId);

    /**
     * Find comments by user
     */
    Page<Comment> findByUserId(Long userId, Pageable pageable);

    /**
     * Count comments on a post
     */
    long countByPostId(Long postId);

    /**
     * Count comments on an answer
     */
    long countByAnswerId(Long answerId);

    /**
     * Find all comments on posts by user
     */
    @Query("SELECT c FROM Comment c WHERE c.postId IN " +
           "(SELECT p.id FROM Post p WHERE p.userId = :userId)")
    Page<Comment> findCommentsOnUserPosts(@Param("userId") Long userId, Pageable pageable);
}

