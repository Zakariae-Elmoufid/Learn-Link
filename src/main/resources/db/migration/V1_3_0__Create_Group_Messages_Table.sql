-- V1_3_0__Create_Group_Messages_Table.sql
-- DEPRECATED: Group messaging feature has been removed from the project

-- The following tables are no longer created due to feature removal:
-- - group_messages (was used for storing messages in study groups)
-- - group_message_read_status (was used for tracking message read receipts)

-- This migration is kept as a placeholder to maintain database migration history
COMMENT ON COLUMN group_message_read_status.read_at IS 'Timestamp when the message was read';
