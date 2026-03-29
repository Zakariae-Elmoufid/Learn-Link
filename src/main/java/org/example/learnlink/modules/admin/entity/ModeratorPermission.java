package org.example.learnlink.modules.admin.entity;

/**
 * Enum defining available moderator permissions
 */
public enum ModeratorPermission {
    // Content Moderation
    HIDE_POSTS,           // Can hide posts
    HIDE_COMMENTS,        // Can hide comments
    HIDE_QUESTIONS,       // Can hide questions
    HIDE_ANSWERS,         // Can hide answers
    
    // User Management (limited)
    VIEW_USER_DETAILS,    // Can view user details
    WARN_USERS,           // Can send warnings to users
    
    // Reports
    VIEW_REPORTS,         // Can view reported content
    RESOLVE_REPORTS       // Can mark reports as resolved
}
