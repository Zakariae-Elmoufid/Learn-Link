package org.example.learnlink.modules.notification.entity;

/**
 * Types of notifications in the system
 */
public enum NotificationType {
    // Task related
    TASK_REMINDER("Task Reminder", "tasks"),
    TASK_DUE("Task Due", "tasks"),

    // Connection related
    CONNECTION_REQUEST("Connection Request", "connections"),
    CONNECTION_ACCEPTED("Connection Accepted", "connections"),

    // Messaging
    NEW_MESSAGE("New Message", "messages"),

    // Community
    POST_LIKED("Post Liked", "community"),
    POST_COMMENTED("Post Commented", "community"),
    QUESTION_ANSWERED("Question Answered", "community"),
    ANSWER_ACCEPTED("Answer Accepted", "community"),
    ANSWER_VOTED("Answer Upvoted", "community"),

    // Gamification
    BADGE_EARNED("Badge Earned", "gamification"),
    POINTS_EARNED("Points Earned", "gamification"),
    LEVEL_UP("Level Up", "gamification"),

    // Groups & Sessions
    GROUP_INVITATION("Group Invitation", "groups"),
    SESSION_REMINDER("Study Session Reminder", "groups"),

    // Admin/Moderation
    MODERATION_ACTION("Moderation Action", "system"),

    // System
    SYSTEM("System Notification", "system");

    private final String displayName;
    private final String category;

    NotificationType(String displayName, String category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategory() {
        return category;
    }
}
