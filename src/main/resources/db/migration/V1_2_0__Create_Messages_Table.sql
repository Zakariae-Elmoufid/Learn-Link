-- V1_2_0__Create_Messages_Table.sql
-- Create messages table for real-time messaging

CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    content VARCHAR(4000) NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    status VARCHAR(20) NOT NULL DEFAULT 'SENT',
    attachment_url VARCHAR(500),
    attachment_name VARCHAR(255),
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_message_sender ON messages(sender_id);
CREATE INDEX idx_message_recipient ON messages(recipient_id);
CREATE INDEX idx_message_conversation ON messages(sender_id, recipient_id);
CREATE INDEX idx_message_created_at ON messages(created_at);
CREATE INDEX idx_message_status ON messages(status);

-- Add comments for documentation
COMMENT ON TABLE messages IS 'Stores chat messages between users';
COMMENT ON COLUMN messages.sender_id IS 'ID of the user who sent the message';
COMMENT ON COLUMN messages.recipient_id IS 'ID of the user who receives the message';
COMMENT ON COLUMN messages.content IS 'The message content (up to 4000 characters)';
COMMENT ON COLUMN messages.message_type IS 'Type of message: TEXT, IMAGE, FILE, AUDIO, VIDEO';
COMMENT ON COLUMN messages.status IS 'Message status: SENT, DELIVERED, READ';
COMMENT ON COLUMN messages.attachment_url IS 'URL to attached file if any';
COMMENT ON COLUMN messages.attachment_name IS 'Original filename of attachment';
COMMENT ON COLUMN messages.read_at IS 'Timestamp when message was read';
