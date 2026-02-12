package org.example.learnlink.modules.community.service;

import org.example.learnlink.modules.community.dto.AddCommentRequest;
import org.example.learnlink.modules.community.dto.CommentResponse;
import org.example.learnlink.modules.community.entity.Comment;
import org.example.learnlink.modules.community.entity.Post;
import org.example.learnlink.modules.community.event.CommentAddedEvent;
import org.example.learnlink.modules.community.mapper.CommentMapper;
import org.example.learnlink.modules.community.repository.CommentRepository;
import org.example.learnlink.modules.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CommentService
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommentResponse addCommentToPost(Long postId, Long userId, AddCommentRequest request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        Comment comment = Comment.builder()
            .postId(postId)
            .userId(userId)
            .content(request.getContent())
            .build();

        comment = commentRepository.save(comment);
        post.incrementCommentsCount();
        postRepository.save(post);

        // Publish event for gamification
        eventPublisher.publishEvent(new CommentAddedEvent(this, userId, comment.getId(), postId, null));

        return commentMapper.commentToResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse addCommentToAnswer(Long answerId, Long userId, AddCommentRequest request) {
        // Verify answer exists
        // Assuming there's an AnswerRepository method, for now we just create the comment

        Comment comment = Comment.builder()
            .answerId(answerId)
            .userId(userId)
            .content(request.getContent())
            .build();

        comment = commentRepository.save(comment);

        // Publish event for gamification
        eventPublisher.publishEvent(new CommentAddedEvent(this, userId, comment.getId(), null, answerId));

        return commentMapper.commentToResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        return commentMapper.commentToResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsForPost(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId)
            .stream()
            .map(commentMapper::commentToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsForAnswer(Long answerId) {
        return commentRepository.findByAnswerIdOrderByCreatedAtDesc(answerId)
            .stream()
            .map(commentMapper::commentToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getUserComments(Long userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable)
            .map(commentMapper::commentToResponse);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, AddCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only comment author can update");
        }

        comment.setContent(request.getContent());

        comment = commentRepository.save(comment);
        return commentMapper.commentToResponse(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only comment author can delete");
        }

        // Decrement comment count on post if applicable
        if (comment.getPostId() != null) {
            Post post = postRepository.findById(comment.getPostId())
                .orElse(null);
            if (post != null) {
                post.decrementCommentsCount();
                postRepository.save(post);
            }
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        comment.incrementLikesCount();
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        comment.decrementLikesCount();
        commentRepository.save(comment);
    }
}

