-- =============================================
-- Migration: V1_4_0__Create_Platform_Stats_Table.sql
-- Description: Creates the platform_stats table for admin dashboard statistics
-- Author: Admin Module Implementation
-- Date: 2024
-- =============================================

-- Create platform_stats table for aggregated platform statistics
CREATE TABLE IF NOT EXISTS platform_stats (
    id BIGINT PRIMARY KEY,
    
    -- User Statistics
    total_users BIGINT NOT NULL DEFAULT 0,
    active_users_last_7_days BIGINT NOT NULL DEFAULT 0,
    active_users_last_30_days BIGINT NOT NULL DEFAULT 0,
    new_users_this_week BIGINT NOT NULL DEFAULT 0,
    new_users_this_month BIGINT NOT NULL DEFAULT 0,
    
    -- Content Statistics
    total_posts BIGINT NOT NULL DEFAULT 0,
    total_questions BIGINT NOT NULL DEFAULT 0,
    total_answers BIGINT NOT NULL DEFAULT 0,
    total_comments BIGINT NOT NULL DEFAULT 0,
    posts_this_week BIGINT NOT NULL DEFAULT 0,
    
    -- Task Statistics
    total_tasks BIGINT NOT NULL DEFAULT 0,
    completed_tasks BIGINT NOT NULL DEFAULT 0,
    
    -- Engagement Statistics
    total_connections BIGINT NOT NULL DEFAULT 0,
    total_study_groups BIGINT NOT NULL DEFAULT 0,
    active_study_groups BIGINT NOT NULL DEFAULT 0,
    
    -- Gamification Statistics
    total_points_awarded BIGINT NOT NULL DEFAULT 0,
    total_badges_earned BIGINT NOT NULL DEFAULT 0,
    
    -- Metadata
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    week_reset_at TIMESTAMP,
    month_reset_at TIMESTAMP
);

-- Insert initial stats record with id=1 (single row pattern)
INSERT INTO platform_stats (id, last_updated) 
VALUES (1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Add comment for documentation
COMMENT ON TABLE platform_stats IS 'Aggregated platform statistics for admin dashboard. Uses single-row pattern with id=1.';
COMMENT ON COLUMN platform_stats.total_users IS 'Total registered users on the platform';
COMMENT ON COLUMN platform_stats.active_users_last_7_days IS 'Users active in the last 7 days based on activity';
COMMENT ON COLUMN platform_stats.active_users_last_30_days IS 'Users active in the last 30 days based on activity';
COMMENT ON COLUMN platform_stats.new_users_this_week IS 'New user registrations this week (reset weekly)';
COMMENT ON COLUMN platform_stats.new_users_this_month IS 'New user registrations this month (reset monthly)';
COMMENT ON COLUMN platform_stats.last_updated IS 'Timestamp of last stats update';
COMMENT ON COLUMN platform_stats.week_reset_at IS 'Timestamp of last weekly counter reset';
COMMENT ON COLUMN platform_stats.month_reset_at IS 'Timestamp of last monthly counter reset';
