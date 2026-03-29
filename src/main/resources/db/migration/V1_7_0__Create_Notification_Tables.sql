-- Notification types enum values
-- TASK_REMINDER, TASK_DUE, CONNECTION_REQUEST, CONNECTION_ACCEPTED,
-- NEW_MESSAGE, POST_LIKED, POST_COMMENTED, QUESTION_ANSWERED,
-- ANSWER_ACCEPTED, BADGE_EARNED, POINTS_EARNED, LEVEL_UP,
-- GROUP_INVITATION, SESSION_REMINDER, MODERATION_ACTION, SYSTEM

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    data JSONB,  -- Additional data (link, entity_id, etc.)
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_type ON notifications(type);
