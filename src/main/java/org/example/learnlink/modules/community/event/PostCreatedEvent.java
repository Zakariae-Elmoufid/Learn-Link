package org.example.learnlink.modules.community.event;

import org.example.learnlink.modules.community.entity.PostType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a post is created
 */
@Getter
public class PostCreatedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long postId;
    private final PostType postType;

    public PostCreatedEvent(Object source, Long userId, Long postId, PostType postType) {
        super(source);
        this.userId = userId;
        this.postId = postId;
        this.postType = postType;
    }
}

