package org.example.learnlink.modules.auth.event;

import org.example.learnlink.modules.auth.entity.User;
import org.springframework.context.ApplicationEvent;

public class OnUserRegisteredEvent extends ApplicationEvent {

    private final User user;

    public OnUserRegisteredEvent(User user) {
        super(user);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}