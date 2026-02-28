package org.example.learnlink.modules.auth.event;

import lombok.Getter;
import org.example.learnlink.modules.auth.entity.User;
import org.springframework.context.ApplicationEvent;

@Getter
public class OnUserRegisteredEvent extends ApplicationEvent {

    private final User user;

    public OnUserRegisteredEvent(User user) {
        super(user);
        this.user = user;
    }

}