package org.example.learnlink.modules.community.service;

import org.example.learnlink.modules.community.dto.CreatePostRequest;
import org.example.learnlink.modules.community.dto.PostResponse;
import org.example.learnlink.modules.community.dto.SearchPostRequest;
import org.example.learnlink.modules.community.dto.UpdatePostRequest;
import org.example.learnlink.modules.community.entity.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface for post service
 */
public interface IPostService {

    /**
     * Create a new post
     */
    PostResponse createPost(Long userId, CreatePostRequest request);

    /**
     * Get post by ID
     */
    PostResponse getPostById(Long postId, Long currentUserId);

    /**
     * Get all posts for a user
     */
    Page<PostResponse> getUserPosts(Long userId, Pageable pageable, Long currentUserId);


    /**
     * Get posts by category
     */
    Page<PostResponse> getPostsByCategory(PostCategory category, Pageable pageable, Long currentUserId);

    /**
     * Get popular posts
     */
    Page<PostResponse> getPopularPosts(Pageable pageable, Long currentUserId);

    /**
     * Get trending posts
     */
    Page<PostResponse> getTrendingPosts(Pageable pageable, Long currentUserId);

    /**
     * Search posts
     */
    Page<PostResponse> searchPosts(SearchPostRequest request, Pageable pageable, Long currentUserId);

    /**
     * Update a post
     */
    PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request);

    /**
     * Delete a post
     */
    void deletePost(Long postId, Long userId);

    /**
     * Like a post
     */
    void likePost(Long postId, Long userId);

    /**
     * Unlike a post
     */
    void unlikePost(Long postId, Long userId);

    /**
     * Get all posts
     */
    Page<PostResponse> getAllPosts(Pageable pageable, Long currentUserId);
}

