package org.example.learnlink.modules.messaging.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a chat message between users
 */
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_message_sender", columnList = "sender_id"),
    @Index(name = "idx_message_recipient", columnList = "recipient_id"),
    @Index(name = "idx_message_conversation", columnList = "sender_id, recipient_id"),
    @Index(name = "idx_message_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    @Column(name = "attachment_key")
    private String attachmentKey;

    @Column(name = "attachment_name")
    private String attachmentName;

    @Column(name = "read_at")
    private LocalDateTime readAt;

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
     * Mark message as delivered
     */
    public void markAsDelivered() {
        this.status = MessageStatus.DELIVERED;
    }

    /**
     * Mark message as read
     */
    public void markAsRead() {
        this.status = MessageStatus.READ;
        this.readAt = LocalDateTime.now();
    }
}
