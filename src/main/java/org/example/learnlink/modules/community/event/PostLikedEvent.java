package org.example.learnlink.modules.community.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Event published when a post is liked
 */
@Getter
public class PostLikedEvent extends ApplicationEvent {
    private final UUID postId;
    private final Long postAuthorId;
    private final Long likerUserId;

    public PostLikedEvent(Object source, UUID postId, Long postAuthorId, Long likerUserId) {
        super(source);
        this.postId = postId;
        this.postAuthorId = postAuthorId;
        this.likerUserId = likerUserId;
    }
}

