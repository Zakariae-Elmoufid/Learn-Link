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

        return mapToResponseWithLikeStatus(post, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findByIdAndHiddenIsFalse(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Increment view count
        post.incrementViewCount();

        postRepository.save(post);
        System.out.println("Post view count incremented for postId: " + postId + ", createdAT : " + post.getCreatedAt());
        return mapToResponseWithLikeStatus(post, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getUserPosts(Long userId, Pageable pageable, Long currentUserId) {
        return postRepository.findByUserIdAndHiddenIsFalse(userId, pageable)
            .map(post -> mapToResponseWithLikeStatus(post, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByCategory(PostCategory category, Pageable pageable, Long currentUserId) {
        return postRepository.findByCategoryAndHiddenIsFalse(category, pageable)
            .map(post -> mapToResponseWithLikeStatus(post, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPopularPosts(Pageable pageable, Long currentUserId) {
        return postRepository.findPopularPostsAndHiddenIsFalse(pageable)
            .map(post -> mapToResponseWithLikeStatus(post, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getTrendingPosts(Pageable pageable, Long currentUserId) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return postRepository.findTrendingPostsAndHiddenIsFalse(since, pageable)
            .map(post -> mapToResponseWithLikeStatus(post, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(SearchPostRequest request, Pageable pageable, Long currentUserId) {
        if (request.getKeyword() != null || request.getCategory() != null || request.getType() != null) {
            return postRepository.searchWithFilters(
                request.getKeyword(),
                request.getCategory(),
                request.getType(),
                pageable
            ).map(post -> mapToResponseWithLikeStatus(post, currentUserId));
        }
        return getAllPosts(pageable, currentUserId);
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
        return mapToResponseWithLikeStatus(post, userId);
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
    public Page<PostResponse> getAllPosts(Pageable pageable, Long currentUserId) {
        return postRepository.findByHiddenFalse(pageable)
            .map(post -> mapToResponseWithLikeStatus(post, currentUserId));
    }


    private PostResponse mapToResponseWithLikeStatus(Post post, Long currentUserId) {
        PostResponse response = postMapper.postToResponse(post);
        boolean likedByCurrentUser = currentUserId != null
            && postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        response.setLikedByCurrentUser(likedByCurrentUser);
        return response;
    }
}

