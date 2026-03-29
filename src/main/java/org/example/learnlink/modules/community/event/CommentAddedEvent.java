package org.example.learnlink.modules.community.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a comment is added
 */
@Getter
public class CommentAddedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long commentId;
    private final Long postId; // null if comment is on answer
    private final Long answerId; // null if comment is on post

    public CommentAddedEvent(Object source, Long userId, Long commentId, Long postId, Long answerId) {
        super(source);
        this.userId = userId;
        this.commentId = commentId;
        this.postId = postId;
        this.answerId = answerId;
    }
}

