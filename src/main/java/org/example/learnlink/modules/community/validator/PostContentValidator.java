package org.example.learnlink.modules.community.validator;

import org.springframework.stereotype.Component;

/**
 * Validator for post content
 */
@Component
public class PostContentValidator {

    private static final int MIN_TITLE_LENGTH = 5;
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MIN_CONTENT_LENGTH = 10;
    private static final int MAX_CONTENT_LENGTH = 5000;

    /**
     * Validate post title
     */
    public void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (title.length() < MIN_TITLE_LENGTH) {
            throw new IllegalArgumentException("Title must be at least " + MIN_TITLE_LENGTH + " characters");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Title must not exceed " + MAX_TITLE_LENGTH + " characters");
        }
    }

    /**
     * Validate post content
     */
    public void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        if (content.length() < MIN_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Content must be at least " + MIN_CONTENT_LENGTH + " characters");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Content must not exceed " + MAX_CONTENT_LENGTH + " characters");
        }
    }

    /**
     * Check for XSS attempts (basic check)
     */
    public void validateContentForXSS(String content) {
        if (containsSuspiciousPatterns(content)) {
            throw new IllegalArgumentException("Content contains potentially harmful patterns");
        }
    }

    /**
     * Check for suspicious HTML/JavaScript patterns
     */
    private boolean containsSuspiciousPatterns(String content) {
        return content.toLowerCase().contains("<script>")
            || content.toLowerCase().contains("javascript:")
            || content.toLowerCase().contains("onerror=")
            || content.toLowerCase().contains("onload=");
    }
}

