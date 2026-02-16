package org.example.learnlink.modules.gamification.exception;

public class UserScoreNotFoundException extends RuntimeException {
    public UserScoreNotFoundException(String message) {
        super(message);
    }

    public UserScoreNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

