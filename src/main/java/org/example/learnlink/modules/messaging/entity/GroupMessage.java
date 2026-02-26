package org.example.learnlink.modules.messaging.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a message in a study group chat.
 * Uses groupId to reference the study group without direct entity dependency.
 */
@Entity
@Table(name = "group_messages", indexes = {
    @Index(name = "idx_group_msg_group_id", columnList = "group_id"),
    @Index(name = "idx_group_msg_sender_id", columnList = "sender_id"),
    @Index(name = "idx_group_msg_created_at", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID of the study group this message belongs to
     */
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    /**
     * ID of the user who sent the message
     */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /**
     * Message content (up to 4000 characters)
     */
    @Column(nullable = false, length = 4000)
    private String content;

    /**
     * Type of message: TEXT, IMAGE, FILE, AUDIO, VIDEO
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    /**
     * URL to attached file (if any)
     */
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    /**
     * Original filename of attachment
     */
    @Column(name = "attachment_name", length = 255)
    private String attachmentName;

    /**
     * Read statuses for this message
     */
    @OneToMany(mappedBy = "groupMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupMessageReadStatus> readStatuses = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if a specific user has read this message
     */
    public boolean isReadByUser(Long userId) {
        return readStatuses.stream()
                .anyMatch(rs -> rs.getUserId().equals(userId));
    }

    /**
     * Get the count of users who have read this message
     */
    public int getReadCount() {
        return readStatuses.size();
    }
}
