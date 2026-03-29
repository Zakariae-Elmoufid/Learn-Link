-- V1_6_0 Add Soft Delete fields to Community tables and create Moderation Logs table
-- Migration for content moderation feature

-- Add soft delete columns to community_posts table
ALTER TABLE community_posts 
ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE community_posts 
ADD COLUMN IF NOT EXISTS hidden_at TIMESTAMP;

ALTER TABLE community_posts 
ADD COLUMN IF NOT EXISTS hidden_by BIGINT;

ALTER TABLE community_posts 
ADD COLUMN IF NOT EXISTS hidden_reason VARCHAR(500);

-- Add soft delete columns to community_comments table
ALTER TABLE community_comments 
ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE community_comments 
ADD COLUMN IF NOT EXISTS hidden_at TIMESTAMP;

ALTER TABLE community_comments 
ADD COLUMN IF NOT EXISTS hidden_by BIGINT;

ALTER TABLE community_comments 
ADD COLUMN IF NOT EXISTS hidden_reason VARCHAR(500);

-- Add soft delete columns to community_questions table
ALTER TABLE community_questions 
ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE community_questions 
ADD COLUMN IF NOT EXISTS hidden_at TIMESTAMP;

ALTER TABLE community_questions 
ADD COLUMN IF NOT EXISTS hidden_by BIGINT;

ALTER TABLE community_questions 
ADD COLUMN IF NOT EXISTS hidden_reason VARCHAR(500);

-- Add soft delete columns to community_answers table
ALTER TABLE community_answers 
ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE community_answers 
ADD COLUMN IF NOT EXISTS hidden_at TIMESTAMP;

ALTER TABLE community_answers 
ADD COLUMN IF NOT EXISTS hidden_by BIGINT;

ALTER TABLE community_answers 
ADD COLUMN IF NOT EXISTS hidden_reason VARCHAR(500);

-- Create moderation_logs table for audit trail
CREATE TABLE IF NOT EXISTS moderation_logs (
    id BIGSERIAL PRIMARY KEY,
    moderator_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    target_user_id BIGINT,
    reason VARCHAR(500),
    content_snapshot TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_moderation_log_moderator FOREIGN KEY (moderator_id) REFERENCES users(id),
    CONSTRAINT fk_moderation_log_target_user FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_posts_hidden ON community_posts(hidden);
CREATE INDEX IF NOT EXISTS idx_comments_hidden ON community_comments(hidden);
CREATE INDEX IF NOT EXISTS idx_questions_hidden ON community_questions(hidden);
CREATE INDEX IF NOT EXISTS idx_answers_hidden ON community_answers(hidden);

CREATE INDEX IF NOT EXISTS idx_moderation_logs_moderator ON moderation_logs(moderator_id);
CREATE INDEX IF NOT EXISTS idx_moderation_logs_action_type ON moderation_logs(action_type);
CREATE INDEX IF NOT EXISTS idx_moderation_logs_target_type ON moderation_logs(target_type);
CREATE INDEX IF NOT EXISTS idx_moderation_logs_target_user ON moderation_logs(target_user_id);
CREATE INDEX IF NOT EXISTS idx_moderation_logs_created_at ON moderation_logs(created_at);

-- Foreign key constraints for hidden_by columns
ALTER TABLE community_posts 
ADD CONSTRAINT IF NOT EXISTS fk_posts_hidden_by FOREIGN KEY (hidden_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE community_comments 
ADD CONSTRAINT IF NOT EXISTS fk_comments_hidden_by FOREIGN KEY (hidden_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE community_questions 
ADD CONSTRAINT IF NOT EXISTS fk_questions_hidden_by FOREIGN KEY (hidden_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE community_answers 
ADD CONSTRAINT IF NOT EXISTS fk_answers_hidden_by FOREIGN KEY (hidden_by) REFERENCES users(id) ON DELETE SET NULL;

-- Comments
COMMENT ON TABLE moderation_logs IS 'Audit trail for all moderation actions';
COMMENT ON COLUMN moderation_logs.action_type IS 'Type of moderation action (POST_HIDDEN, POST_RESTORED, etc.)';
COMMENT ON COLUMN moderation_logs.target_type IS 'Type of content being moderated (POST, COMMENT, QUESTION, ANSWER)';
COMMENT ON COLUMN moderation_logs.content_snapshot IS 'Snapshot of deleted content for audit purposes';
