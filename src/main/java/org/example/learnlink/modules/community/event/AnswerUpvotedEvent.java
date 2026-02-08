package org.example.learnlink.modules.community.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an answer receives a vote
 */
@Getter
public class AnswerUpvotedEvent extends ApplicationEvent {

    private final Long answerId;
    private final Long answerAuthorId;
    private final Long voterUserId;

    public AnswerUpvotedEvent(Object source, Long answerId, Long answerAuthorId, Long voterUserId) {
        super(source);
        this.answerId = answerId;
        this.answerAuthorId = answerAuthorId;
        this.voterUserId = voterUserId;
    }
}

