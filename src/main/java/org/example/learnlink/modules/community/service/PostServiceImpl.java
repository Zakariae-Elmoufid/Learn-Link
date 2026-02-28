package org.example.learnlink.modules.community.service;

import org.example.learnlink.common.service.RedisService;
import org.example.learnlink.modules.community.dto.CreatePostRequest;
import org.example.learnlink.modules.community.dto.PostResponse;
import org.example.learnlink.modules.community.dto.SearchPostRequest;
import org.example.learnlink.modules.community.dto.UpdatePostRequest;
import org.example.learnlink.modules.community.entity.Post;
import org.example.learnlink.modules.community.entity.PostCategory;
import org.example.learnlink.modules.community.entity.PostLike;
import org.example.learnlink.modules.community.event.PostCreatedEvent;
import org.example.learnlink.modules.community.mapper.PostMapper;
import org.example.learnlink.modules.community.repository.PostLikeRepository;
import org.example.learnlink.modules.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of PostService
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements IPostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostMapper postMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request) {
        Post post = Post.builder()
            .userId(userId)
            .title(request.getTitle())
            .content(request.getContent())
            .type(request.getType())
            .category(request.getCategory())
            .build();

        post = postRepository.save(post);

        // Publish event for gamification
        eventPublisher.publishEvent(new PostCreatedEvent(this, userId, post.getId(), request.getType()));

        return postMapper.postToResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Increment view count
        post.incrementViewCount();

        postRepository.save(post);
        System.out.println("Post view count incremented for postId: " + postId + ", createdAT : " + post.getCreatedAt());
        return postMapper.postToResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getUserPosts(Long userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable)
            .map(postMapper::postToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByCategory(PostCategory category, Pageable pageable) {
        return postRepository.findByCategory(category, pageable)
            .map(postMapper::postToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPopularPosts(Pageable pageable) {
        return postRepository.findPopularPosts(pageable)
            .map(postMapper::postToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getTrendingPosts(Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return postRepository.findTrendingPosts(since, pageable)
            .map(postMapper::postToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(SearchPostRequest request, Pageable pageable) {
        if (request.getKeyword() != null || request.getCategory() != null || request.getType() != null) {
            return postRepository.searchWithFilters(
                request.getKeyword(),
                request.getCategory(),
                request.getType(),
                pageable
            ).map(postMapper::postToResponse);
        }
        return getAllPosts(pageable);
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only post author can update");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());

        post = postRepository.save(post);
        return postMapper.postToResponse(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only post author can delete");
        }

        postRepository.delete(post);
    }

    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new RuntimeException("User already liked this post");
        }

        PostLike like = PostLike.builder()
            .postId(postId)
            .userId(userId)
            .build();

        postLikeRepository.save(like);
        post.incrementLikesCount();
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
        post.decrementLikesCount();
        postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
            .map(postMapper::postToResponse);
    }
}

