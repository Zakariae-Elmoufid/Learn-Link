package org.example.learnlink.modules.admin.entity;

/**
 * Enum representing the types of moderation actions
 */
public enum ModerationActionType {
    // Post actions
    POST_HIDDEN,
    POST_RESTORED,
    POST_PERMANENTLY_DELETED,
    
    // Comment actions
    COMMENT_HIDDEN,
    COMMENT_RESTORED,
    COMMENT_PERMANENTLY_DELETED,
    
    // Question actions
    QUESTION_HIDDEN,
    QUESTION_RESTORED,
    QUESTION_PERMANENTLY_DELETED,
    
    // Answer actions
    ANSWER_HIDDEN,
    ANSWER_RESTORED,
    ANSWER_PERMANENTLY_DELETED,
    
    // User actions
    USER_ACTIVATED,
    USER_DEACTIVATED,
    USER_BANNED,
    
    // Moderator actions
    MODERATOR_CREATED,
    MODERATOR_PERMISSIONS_UPDATED,
    MODERATOR_REMOVED
}
