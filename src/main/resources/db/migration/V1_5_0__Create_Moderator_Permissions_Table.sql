-- V1_5_0 Create Moderator Permissions table
-- Migration for storing moderator permissions and assignments

CREATE TABLE IF NOT EXISTS moderator_permissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    assigned_by BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    notes TEXT,
    CONSTRAINT fk_moderator_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_assigned_by_user FOREIGN KEY (assigned_by) REFERENCES users(id)
);

-- Table for storing moderator permission values (ElementCollection)
CREATE TABLE IF NOT EXISTS moderator_permission_list (
    moderator_permission_id BIGINT NOT NULL,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (moderator_permission_id, permission),
    CONSTRAINT fk_moderator_permission FOREIGN KEY (moderator_permission_id) 
        REFERENCES moderator_permissions(id) ON DELETE CASCADE
);

-- Index for faster lookups
CREATE INDEX idx_moderator_permissions_user_id ON moderator_permissions(user_id);
CREATE INDEX idx_moderator_permissions_assigned_by ON moderator_permissions(assigned_by);

COMMENT ON TABLE moderator_permissions IS 'Stores moderator assignments and their permission metadata';
COMMENT ON TABLE moderator_permission_list IS 'Stores individual permissions granted to each moderator';
