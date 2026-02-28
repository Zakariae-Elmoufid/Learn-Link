package org.example.learnlink.modules.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.common.exception.ResourceNotFoundException;
import org.example.learnlink.modules.admin.dto.request.ModerationActionRequest;
import org.example.learnlink.modules.admin.dto.response.*;
import org.example.learnlink.modules.admin.entity.ModerationActionType;
import org.example.learnlink.modules.admin.entity.ModerationLog;
import org.example.learnlink.modules.admin.entity.ModerationTargetType;
import org.example.learnlink.modules.admin.repository.ModerationLogRepository;
import org.example.learnlink.modules.auth.repository.UserRepository;
import org.example.learnlink.modules.community.entity.Answer;
import org.example.learnlink.modules.community.entity.Comment;
import org.example.learnlink.modules.community.entity.Post;
import org.example.learnlink.modules.community.entity.Question;
import org.example.learnlink.modules.community.repository.AnswerRepository;
import org.example.learnlink.modules.community.repository.CommentRepository;
import org.example.learnlink.modules.community.repository.PostRepository;
import org.example.learnlink.modules.community.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of AdminModerationService for content moderation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminModerationServiceImpl implements AdminModerationService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final ModerationLogRepository moderationLogRepository;
    private final UserRepository userRepository;

    // ==================== POST MODERATION ====================

    @Override
    @Transactional(readOnly = true)
    public Page<PostModerationDto> getAllPostsForModeration(Pageable pageable) {
        log.info("Fetching all posts for moderation");
        return postRepository.findAll(pageable).map(this::mapToPostModerationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostModerationDto> getHiddenPosts(Pageable pageable) {
        log.info("Fetching hidden posts");
        return postRepository.findByHiddenTrue(pageable).map(this::mapToPostModerationDto);
    }

    @Override
    @Transactional
    public ModerationActionResponse hidePost(Long postId, Long moderatorId, ModerationActionRequest request) {
        log.info("Moderator {} hiding post {}", moderatorId, postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));
        
        if (Boolean.TRUE.equals(post.getHidden())) {
            throw new IllegalStateException("Post is already hidden");
        }
        
        // Soft delete
        post.setHidden(true);
        post.setHiddenAt(LocalDateTime.now());
        post.setHiddenBy(moderatorId);
        post.setHiddenReason(request.getReason());
        postRepository.save(post);
        
        // Log moderation action
        ModerationLog moderationLog = createModerationLog(
                moderatorId,
                ModerationActionType.POST_HIDDEN,
                ModerationTargetType.POST,
                postId,
                post.getUserId(),
                request.getReason(),
                truncateContent(post.getContent())
        );
        
        return mapToModerationActionResponse(moderationLog, request.getNotifyUser());
    }

    @Override
    @Transactional
    public ModerationActionResponse restorePost(Long postId, Long adminId, String reason) {
        log.info("Admin {} restoring post {}", adminId, postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));
        
        if (Boolean.FALSE.equals(post.getHidden())) {
            throw new IllegalStateException("Post is not hidden");
        }
        
        // Restore
        post.setHidden(false);
        post.setHiddenAt(null);
        post.setHiddenBy(null);
        post.setHiddenReason(null);
        postRepository.save(post);
        
        // Log moderation action
        ModerationLog moderationLog = createModerationLog(
                adminId,
                ModerationActionType.POST_RESTORED,
                ModerationTargetType.POST,
                postId,
                post.getUserId(),
                reason,
                null
        );
        
        return mapToModerationActionResponse(moderationLog, false);
    }

    @Override
    @Transactional
    public ModerationActionResponse permanentlyDeletePost(Long postId, Long adminId, ModerationActionRequest request) {
        log.info("Admin {} permanently deleting post {}", adminId, postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));
        
        Long targetUserId = post.getUserId();
        String contentSnapshot = truncateContent(post.getContent());
        
        // Permanently delete
        postRepository.delete(post);
        
        // Log moderation action
        ModerationLog moderationLog = createModerationLog(
                adminId,
                ModerationActionType.POST_PERMANENTLY_DELETED,
                ModerationTargetType.POST,
                postId,
                targetUserId,
                request.getReason(),
                contentSnapshot
        );
        
        return mapToModerationActionResponse(moderationLog, request.getNotifyUser());
    }

    // ==================== COMMENT MODERATION ====================

    @Override
    @Transactional(readOnly = true)
    public Page<CommentModerationDto> getAllCommentsForModeration(Pageable pageable) {
        log.info("Fetching all comments for moderation");
        return commentRepository.findAll(pageable).map(this::mapToCommentModerationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentModerationDto> getHiddenComments(Pageable pageable) {
        log.info("Fetching hidden comments");
        return commentRepository.findByHiddenTrue(pageable).map(this::mapToCommentModerationDto);
    }

    @Override
    @Transactional
    public ModerationActionResponse hideComment(Long commentId, Long moderatorId, ModerationActionRequest request) {
        log.info("Moderator {} hiding comment {}", moderatorId, commentId);
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));
        
        if (Boolean.TRUE.equals(comment.getHidden())) {
            throw new IllegalStateException("Comment is already hidden");
        }
        
        // Soft delete
        comment.setHidden(true);
        comment.setHiddenAt(LocalDateTime.now());
        comment.setHiddenBy(moderatorId);
        comment.setHiddenReason(request.getReason());
        commentRepository.save(comment);
        
        // Log moderation action
        ModerationLog moderationLog = createModerationLog(
                moderatorId,
                ModerationActionType.COMMENT_HIDDEN,
                ModerationTargetType.COMMENT,
                commentId,
                comment.getUserId(),
                request.getReason(),
                truncateContent(comment.getContent())
        );
        
        return mapToModerationActionResponse(moderationLog, request.getNotifyUser());
    }

    @Override
    @Transactional
    public ModerationActionResponse restoreComment(Long commentId, Long adminId, String reason) {
        log.info("Admin {} restoring comment {}", adminId, commentId);
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));
        
        if (Boolean.FALSE.equals(comment.getHidden())) {
            throw new IllegalStateException("Comment is not hidden");
        }
        
        // Restore
        comment.setHidden(false);
        comment.setHiddenAt(null);
        comment.setHiddenBy(null);
        comment.setHiddenReason(null);
        commentRepository.save(comment);
        
        // Log moderation action
        ModerationLog moderationLog = createModerationLog(
                adminId,
                ModerationActionType.COMMENT_RESTORED,
                ModerationTargetType.COMMENT,
                commentId,
                comment.getUserId(),
                reason,
                null
        );
        
        return mapToModerationActionResponse(moderationLog, false);
    }

    @Override
    @Transactional
    public ModerationActionResponse permanentlyDeleteComment(Long commentId, Long adminId, ModerationActionRequest request) {
        log.info("Admin {} permanently deleting comment {}", adminId, commentId);
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));
        
        Long targetUserId = comment.getUserId();
        String contentSnapshot = truncateContent(comment.getContent());
        
        // Permanently delete
        commentRepository.delete(comment);
        
        // Log moderation action
        ModerationLog moderationLog = createModerationLog(
                adminId,
                ModerationActionType.COMMENT_PERMANENTLY_DELETED,
                ModerationTargetType.COMMENT,
                commentId,
                targetUserId,
                request.getReason(),
                contentSnapshot
        );
        
        return mapToModerationActionResponse(moderationLog, request.getNotifyUser());
    }

    // ==================== QUESTION MODERATION ====================

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionModerationDto> getAllQuestionsForModeration(Pageable pageable) {
        log.info("Fetching all questions for moderation");
        return questionRepository.findAll(pageable).map(this::mapToQuestionModerationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionModerationDto> getHiddenQuestions(Pageable pageable) {
        log.info("Fetching hidden questions");
        return questionRepository.findByHiddenTrue(pageable).map(this::mapToQuestionModerationDto);
    }

    @Override
    @Transactional
    public ModerationActionResponse hideQuestion(Long questionId, Long moderatorId, ModerationActionRequest request) {
        log.info("Moderator {} hiding question {}", moderatorId, questionId);
        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + questionId));
        
        if (Boolean.TRUE.equals(question.getHidden())) {
            throw new IllegalStateException("Question is already hidden");
        }
        
        // Soft delete
        question.setHidden(true);
        question.setHiddenAt(LocalDateTime.now());
        question.setHiddenBy(moderatorId);
        question.setHiddenReason(request.getReason());
        questionRepository.save(question);
        
        // Log moderation action
        ModerationLog moderationLog = createModerationLog(
                moderatorId,
                ModerationActionType.QUESTION_HIDDEN,
                ModerationTargetType.QUESTION,
                questionId,
                question.getUserId(),
                request.getReason(),
                truncateContent(question.getContent())
        );
        
        return mapToModerationActionResponse(moderationLog, request.getNotifyUser());
    }

    @Override
    @Transactional
    public ModerationActionResponse restoreQuestion(Long questionId, Long adminId, String reason) {
        log.info("Admin {} restoring question {}", adminId, questionId);
        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + questionId));
        
        if (Boolean.FALSE.equals(question.getHidden())) {
            throw new IllegalStateException("Question is not hidden");
        }
        
        // Restore
        question.setHidden(false);
        question.setHiddenAt(null);
        question.setHiddenBy(null);
        question.setHiddenReason(null);
        questionRepository.save(question);
        
        // Log moderation action
        ModerationLog moderationLog = createModerationLog(
                adminId,
                ModerationActionType.QUESTION_RESTORED,
                ModerationTargetType.QUESTION,
                questionId,
                question.getUserId(),
                reason,
                null
        );
        
        return mapToModerationActionResponse(moderationLog, false);
    }

    @Override
    @Transactional
    public ModerationActionResponse permanentlyDeleteQuestion(Long questionId, Long adminId, ModerationActionRequest request) {
        log.info("Admin {} permanently deleting question {}", adminId, questionId);
        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + questionId));
        
        Long targetUserId = question.getUserId();
        String contentSnapshot = truncateContent(question.getContent());
        
        // Permanently delete
        questionRepository.delete(question);
        
        // Log moderation action
        ModerationLog moderationLog = createModerationLog(
                adminId,
                ModerationActionType.QUESTION_PERMANENTLY_DELETED,
                ModerationTargetType.QUESTION,
                questionId,
                targetUserId,
                request.getReason(),
                contentSnapshot
        );
        
        return mapToModerationActionResponse(moderationLog, request.getNotifyUser());
    }

    // ==================== ANSWER MODERATION ====================

    @Override
    @Transactional(readOnly = true)
    public Page<AnswerModerationDto> getAllAnswersForModeration(Pageable pageable) {
        log.info("Fetching all answers for moderation");
        return answerRepository.findAll(pageable).map(this::mapToAnswerModerationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnswerModerationDto> getHiddenAnswers(Pageable pageable) {
        log.info("Fetching hidden answers");
        return answerRepository.findByHiddenTrue(pageable).map(this::mapToAnswerModerationDto);
    }

    @Override
    @Transactional
    public ModerationActionResponse hideAnswer(Long answerId, Long moderatorId, ModerationActionRequest request) {
        log.info("Moderator {} hiding answer {}", moderatorId, answerId);
        
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with ID: " + answerId));
        
        if (Boolean.TRUE.equals(answer.getHidden())) {
            throw new IllegalStateException("Answer is already hidden");
        }
        
        // Soft delete
        answer.setHidden(true);
        answer.setHiddenAt(LocalDateTime.now());
        answer.setHiddenBy(moderatorId);
        answer.setHiddenReason(request.getReason());
        answerRepository.save(answer);
        
        // Log moderation action
        ModerationLog moderationLog = createModerationLog(
                moderatorId,
                ModerationActionType.ANSWER_HIDDEN,
                ModerationTargetType.ANSWER,
                answerId,
                answer.getUserId(),
                request.getReason(),
                truncateContent(answer.getContent())
        );
        
        return mapToModerationActionResponse(moderationLog, request.getNotifyUser());
    }

    @Override
    @Transactional
    public ModerationActionResponse restoreAnswer(Long answerId, Long adminId, String reason) {
        log.info("Admin {} restoring answer {}", adminId, answerId);
        
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with ID: " + answerId));
        
        if (Boolean.FALSE.equals(answer.getHidden())) {
            throw new IllegalStateException("Answer is not hidden");
        }
        
        // Restore
        answer.setHidden(false);
        answer.setHiddenAt(null);
        answer.setHiddenBy(null);
        answer.setHiddenReason(null);
        answerRepository.save(answer);
        
        // Log moderation action
        ModerationLog moderationLog = createModerationLog(
                adminId,
                ModerationActionType.ANSWER_RESTORED,
                ModerationTargetType.ANSWER,
                answerId,
                answer.getUserId(),
                reason,
                null
        );
        
        return mapToModerationActionResponse(moderationLog, false);
    }

    @Override
    @Transactional
    public ModerationActionResponse permanentlyDeleteAnswer(Long answerId, Long adminId, ModerationActionRequest request) {
        log.info("Admin {} permanently deleting answer {}", adminId, answerId);
        
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with ID: " + answerId));
        
        Long targetUserId = answer.getUserId();
        String contentSnapshot = truncateContent(answer.getContent());
        
        // Permanently delete
        answerRepository.delete(answer);
        
        // Log moderation action
        ModerationLog moderationLog = createModerationLog(
                adminId,
                ModerationActionType.ANSWER_PERMANENTLY_DELETED,
                ModerationTargetType.ANSWER,
                answerId,
                targetUserId,
                request.getReason(),
                contentSnapshot
        );
        
        return mapToModerationActionResponse(moderationLog, request.getNotifyUser());
    }

    // ==================== MODERATION LOGS ====================

    @Override
    @Transactional(readOnly = true)
    public Page<ModerationLogDto> getModerationLogs(Pageable pageable) {
        log.info("Fetching moderation logs");
        return moderationLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToModerationLogDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModerationLogDto> getModerationLogsByModerator(Long moderatorId, Pageable pageable) {
        log.info("Fetching moderation logs for moderator {}", moderatorId);
        return moderationLogRepository.findByModeratorIdOrderByCreatedAtDesc(moderatorId, pageable)
                .map(this::mapToModerationLogDto);
    }

    // ==================== HELPER METHODS ====================

    private ModerationLog createModerationLog(
            Long moderatorId,
            ModerationActionType actionType,
            ModerationTargetType targetType,
            Long targetId,
            Long targetUserId,
            String reason,
            String contentSnapshot
    ) {
        ModerationLog log = ModerationLog.builder()
                .moderatorId(moderatorId)
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .targetUserId(targetUserId)
                .reason(reason)
                .contentSnapshot(contentSnapshot)
                .build();
        return moderationLogRepository.save(log);
    }

    private String truncateContent(String content) {
        if (content == null) return null;
        return content.length() > 500 ? content.substring(0, 500) + "..." : content;
    }

    private String getUsername(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .map(user -> user.getUsername())
                .orElse("Unknown");
    }

    private PostModerationDto mapToPostModerationDto(Post post) {
        return PostModerationDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .username(getUsername(post.getUserId()))
                .title(post.getTitle())
                .content(post.getContent())
                .type(post.getType())
                .category(post.getCategory())
                .viewCount(post.getViewCount())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .hidden(post.getHidden())
                .hiddenAt(post.getHiddenAt())
                .hiddenBy(post.getHiddenBy())
                .hiddenByUsername(getUsername(post.getHiddenBy()))
                .hiddenReason(post.getHiddenReason())
                .build();
    }

    private CommentModerationDto mapToCommentModerationDto(Comment comment) {
        return CommentModerationDto.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .answerId(comment.getAnswerId())
                .userId(comment.getUserId())
                .username(getUsername(comment.getUserId()))
                .content(comment.getContent())
                .likesCount(comment.getLikesCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .hidden(comment.getHidden())
                .hiddenAt(comment.getHiddenAt())
                .hiddenBy(comment.getHiddenBy())
                .hiddenByUsername(getUsername(comment.getHiddenBy()))
                .hiddenReason(comment.getHiddenReason())
                .build();
    }

    private QuestionModerationDto mapToQuestionModerationDto(Question question) {
        return QuestionModerationDto.builder()
                .id(question.getId())
                .userId(question.getUserId())
                .username(getUsername(question.getUserId()))
                .title(question.getTitle())
                .content(question.getContent())
                .viewCount(question.getViewCount())
                .isResolved(question.getIsResolved())
                .acceptedAnswerId(question.getAcceptedAnswerId())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .hidden(question.getHidden())
                .hiddenAt(question.getHiddenAt())
                .hiddenBy(question.getHiddenBy())
                .hiddenByUsername(getUsername(question.getHiddenBy()))
                .hiddenReason(question.getHiddenReason())
                .build();
    }

    private AnswerModerationDto mapToAnswerModerationDto(Answer answer) {
        return AnswerModerationDto.builder()
                .id(answer.getId())
                .questionId(answer.getQuestionId())
                .userId(answer.getUserId())
                .username(getUsername(answer.getUserId()))
                .content(answer.getContent())
                .voteCount(answer.getVoteCount())
                .upvoteCount(answer.getUpvoteCount())
                .downvoteCount(answer.getDownvoteCount())
                .isAccepted(answer.getIsAccepted())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .hidden(answer.getHidden())
                .hiddenAt(answer.getHiddenAt())
                .hiddenBy(answer.getHiddenBy())
                .hiddenByUsername(getUsername(answer.getHiddenBy()))
                .hiddenReason(answer.getHiddenReason())
                .build();
    }

    private ModerationLogDto mapToModerationLogDto(ModerationLog log) {
        return ModerationLogDto.builder()
                .id(log.getId())
                .moderatorId(log.getModeratorId())
                .moderatorUsername(getUsername(log.getModeratorId()))
                .actionType(log.getActionType())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .targetUserId(log.getTargetUserId())
                .targetUsername(getUsername(log.getTargetUserId()))
                .reason(log.getReason())
                .contentSnapshot(log.getContentSnapshot())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private ModerationActionResponse mapToModerationActionResponse(ModerationLog log, Boolean userNotified) {
        return ModerationActionResponse.builder()
                .id(log.getId())
                .actionType(log.getActionType())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .reason(log.getReason())
                .actionAt(log.getCreatedAt())
                .moderatorId(log.getModeratorId())
                .moderatorUsername(getUsername(log.getModeratorId()))
                .userNotified(userNotified)
                .build();
    }
}
