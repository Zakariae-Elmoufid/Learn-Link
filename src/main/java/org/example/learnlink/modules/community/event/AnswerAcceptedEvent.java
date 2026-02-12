package org.example.learnlink.modules.community.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an answer is accepted as the best answer
 */
@Getter
public class AnswerAcceptedEvent extends ApplicationEvent {

    private final Long answerId;
    private final Long answerAuthorId;
    private final Long questionAskerUserId;

    public AnswerAcceptedEvent(Object source, Long answerId, Long answerAuthorId, Long questionAskerUserId) {
        super(source);
        this.answerId = answerId;
        this.answerAuthorId = answerAuthorId;
        this.questionAskerUserId = questionAskerUserId;
    }
}

