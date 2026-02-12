package org.example.learnlink.modules.community.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a question is asked
 */
@Getter
public class QuestionAskedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long questionId;

    public QuestionAskedEvent(Object source, Long userId, Long questionId) {
        super(source);
        this.userId = userId;
        this.questionId = questionId;
    }
}

