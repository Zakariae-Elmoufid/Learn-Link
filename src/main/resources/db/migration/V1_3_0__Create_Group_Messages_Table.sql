-- V1_3_0__Create_Group_Messages_Table.sql
-- Create tables for group chat messaging feature

-- Group Messages Table
CREATE TABLE IF NOT EXISTS group_messages (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content VARCHAR(4000) NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    attachment_url VARCHAR(500),
    attachment_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to study_groups table
    CONSTRAINT fk_group_message_group 
        FOREIGN KEY (group_id) 
        REFERENCES study_groups(id) 
        ON DELETE CASCADE
);

-- Indexes for group_messages
CREATE INDEX idx_group_msg_group_id ON group_messages(group_id);
CREATE INDEX idx_group_msg_sender_id ON group_messages(sender_id);
CREATE INDEX idx_group_msg_created_at ON group_messages(created_at DESC);
CREATE INDEX idx_group_msg_group_created ON group_messages(group_id, created_at DESC);

-- Group Message Read Status Table
CREATE TABLE IF NOT EXISTS group_message_read_status (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to group_messages table
    CONSTRAINT fk_read_status_message 
        FOREIGN KEY (message_id) 
        REFERENCES group_messages(id) 
        ON DELETE CASCADE,
    
    -- Each user can only have one read status per message
    CONSTRAINT uk_message_user_read 
        UNIQUE (message_id, user_id)
);

-- Indexes for group_message_read_status
CREATE INDEX idx_read_status_message_id ON group_message_read_status(message_id);
CREATE INDEX idx_read_status_user_id ON group_message_read_status(user_id);

-- Comments for documentation
COMMENT ON TABLE group_messages IS 'Stores chat messages within study groups';
COMMENT ON COLUMN group_messages.group_id IS 'Reference to the study group';
COMMENT ON COLUMN group_messages.sender_id IS 'ID of the user who sent the message';
COMMENT ON COLUMN group_messages.content IS 'Message content (up to 4000 characters)';
COMMENT ON COLUMN group_messages.message_type IS 'Type: TEXT, IMAGE, FILE, AUDIO, VIDEO';

COMMENT ON TABLE group_message_read_status IS 'Tracks which users have read which messages';
COMMENT ON COLUMN group_message_read_status.message_id IS 'Reference to the group message';
COMMENT ON COLUMN group_message_read_status.user_id IS 'ID of the user who read the message';
COMMENT ON COLUMN group_message_read_status.read_at IS 'Timestamp when the message was read';
