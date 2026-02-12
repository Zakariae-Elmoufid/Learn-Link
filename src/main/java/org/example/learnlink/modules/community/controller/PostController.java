package org.example.learnlink.modules.community.controller;

import org.example.learnlink.common.service.RedisService;
import org.example.learnlink.modules.community.dto.*;
import org.example.learnlink.modules.community.entity.PostCategory;
import org.example.learnlink.modules.community.mapper.PostMapper;
import org.example.learnlink.modules.community.service.IPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Post management
 */
@RestController@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class PostController {

    private final IPostService postService;
    private final RedisService redisService;
    private final PostMapper mapper;


    /**
     * Create a new post
     * POST /api/community/posts
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreatePostRequest request) {

        PostResponse response = postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get post by ID
     * GET /api/community/posts/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
        String cacheKey = "post:" + postId;
        PostResponse cachedPost = (PostResponse) redisService.get(cacheKey);

        if (cachedPost != null) {
            System.out.println("Cache hit for postId: " );
            return ResponseEntity.ok(cachedPost);
        }
        PostResponse response = postService.getPostById(postId);
        redisService.save(cacheKey, response, 3600);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all posts with pagination
     * GET /api/community/posts?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<PageResponse<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String cacheKey = "posts:page:" + page + ":size:" + size;
        PageResponse<PostResponse> cachedPosts = (PageResponse<PostResponse>) redisService.get(cacheKey);

        if (cachedPosts != null) {
            return ResponseEntity.ok(cachedPosts);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<PostResponse> posts = postService.getAllPosts(pageable);

        PageResponse<PostResponse> response =
                mapper.toPageResponse(posts);

        redisService.save(cacheKey, response, 1800);


        return ResponseEntity.ok(response);
    }

    /**
     * Get posts by category
     * GET /api/community/posts/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<PostResponse>> getPostsByCategory(
            @PathVariable PostCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts = postService.getPostsByCategory(category, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get popular posts
     * GET /api/community/posts/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<Page<PostResponse>> getPopularPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts = postService.getPopularPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get trending posts
     * GET /api/community/posts/trending
     */
    @GetMapping("/trending")
    public ResponseEntity<Page<PostResponse>> getTrendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts = postService.getTrendingPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get user posts
     * GET /api/community/posts/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts = postService.getUserPosts(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Search posts
     * GET /api/community/posts/search?keyword=...&category=...&type=...
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PostCategory category,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        SearchPostRequest request = SearchPostRequest.builder()
            .keyword(keyword)
            .category(category)
            .build();
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts = postService.searchPosts(request, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Update a post
     * PUT /api/community/posts/{postId}
     */
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdatePostRequest request) {
        PostResponse response = postService.updatePost(postId, userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a post
     * DELETE /api/community/posts/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId) {
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Like a post
     * POST /api/community/posts/{postId}/like
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId) {
        postService.likePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Unlike a post
     * DELETE /api/community/posts/{postId}/like
     */
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> unlikePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId) {
        postService.unlikePost(postId, userId);
        return ResponseEntity.ok().build();
    }
}

