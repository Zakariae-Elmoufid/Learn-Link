package org.example.learnlink.modules.messaging.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity tracking which users have read which group messages.
 * Enables read receipts feature for group chats.
 */
@Entity
@Table(name = "group_message_read_status",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_message_user_read",
                columnNames = {"message_id", "user_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMessageReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The message that was read
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private GroupMessage groupMessage;

    /**
     * ID of the user who read the message
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Timestamp when the message was read
     */
    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        if (readAt == null) {
            readAt = LocalDateTime.now();
        }
    }
}
