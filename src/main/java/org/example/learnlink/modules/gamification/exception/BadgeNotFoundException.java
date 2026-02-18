package org.example.learnlink.modules.gamification.exception;

public class BadgeNotFoundException extends RuntimeException {
    public BadgeNotFoundException(String message) {
        super(message);
    }

    public BadgeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

