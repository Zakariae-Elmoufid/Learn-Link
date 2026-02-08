package org.example.learnlink.modules.community.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an answer is provided to a question
 */
@Getter
public class AnswerProvidedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long answerId;
    private final Long questionId;

    public AnswerProvidedEvent(Object source, Long userId, Long answerId, Long questionId) {
        super(source);
        this.userId = userId;
        this.answerId = answerId;
        this.questionId = questionId;
    }
}

