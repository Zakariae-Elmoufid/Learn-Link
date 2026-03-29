package org.example.learnlink.modules.community.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;


/**
 * Event published when a post is liked
 */
@Getter
public class PostLikedEvent extends ApplicationEvent {
    private final Long postId;
    private final Long postAuthorId;
    private final Long likerUserId;

    public PostLikedEvent(Object source, Long postId, Long postAuthorId, Long likerUserId) {
        super(source);
        this.postId = postId;
        this.postAuthorId = postAuthorId;
        this.likerUserId = likerUserId;
    }
}

