-- Create Badges Table
CREATE TABLE badges (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    type VARCHAR(50) NOT NULL,
    rarity VARCHAR(50) NOT NULL,
    points_required INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create User Badges Table
CREATE TABLE user_badges (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    earned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, badge_id),
    FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE
);

-- Create Indexes
CREATE INDEX idx_badges_code ON badges(code);
CREATE INDEX idx_badges_type ON badges(type);
CREATE INDEX idx_badges_rarity ON badges(rarity);
CREATE INDEX idx_badges_active ON badges(active);
CREATE INDEX idx_user_badges_user_id ON user_badges(user_id);
CREATE INDEX idx_user_badges_badge_id ON user_badges(badge_id);
CREATE INDEX idx_user_badges_earned_at ON user_badges(earned_at);

-- Insert Pre-configured Badges
INSERT INTO badges (code, name, description, icon_url, type, rarity, points_required, active) VALUES
('FIRST_POST', 'First Step', 'Publish your first post', 'https://icon.example.com/first_post.png', 'ACTION', 'COMMON', 0, true),
('HELPFUL_EXPERT', 'Helpful Expert', 'Get 50 helpful votes on answers', 'https://icon.example.com/helpful_expert.png', 'ACHIEVEMENT', 'RARE', 500, true),
('COMMUNITY_LEADER', 'Community Leader', 'Earn 1000 points', 'https://icon.example.com/community_leader.png', 'MILESTONE', 'EPIC', 1000, true),
('STREAK_MASTER', 'Streak Master', 'Maintain 30-day streak', 'https://icon.example.com/streak_master.png', 'MILESTONE', 'RARE', 800, true),
('LEVEL_10', 'Legend', 'Reach level 10', 'https://icon.example.com/level_10.png', 'MILESTONE', 'LEGENDARY', 2000, true),
('QUESTION_MASTER', 'Question Master', 'Ask 100 questions', 'https://icon.example.com/question_master.png', 'ACHIEVEMENT', 'RARE', 600, true),
('SOCIAL_BUTTERFLY', 'Social Butterfly', 'Connect with 50 users', 'https://icon.example.com/social_butterfly.png', 'ACHIEVEMENT', 'UNCOMMON', 300, true),
('KNOWLEDGE_SEEKER', 'Knowledge Seeker', 'Complete 5 challenges', 'https://icon.example.com/knowledge_seeker.png', 'ACHIEVEMENT', 'UNCOMMON', 400, true);

