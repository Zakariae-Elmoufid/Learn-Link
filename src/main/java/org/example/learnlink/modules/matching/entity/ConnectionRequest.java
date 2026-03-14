package org.example.learnlink.modules.matching.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnlink.modules.matching.entity.enums.RequestStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a connection request between two users.
 * When a user wants to connect with another, a request is created with PENDING status.
 * The receiver can then accept or reject the request.
 */
@Entity
@Table(name = "connection_requests",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_connection_request_sender_receiver",
                columnNames = {"sender_id", "receiver_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who initiated the connection request
     */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /**
     * The user who receives the connection request
     */
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    /**
     * Optional personal message from the sender
     */
    @Column(length = 500)
    private String message;

    /**
     * Current status of the request (PENDING, ACCEPTED, REJECTED, CANCELLED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    /**
     * Compatibility score between the two users (0-100)
     * Calculated by the matching algorithm when the request is created
     */
    @Column(name = "compatibility_score", precision = 5, scale = 2)
    private BigDecimal compatibilityScore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Check if the request is still pending
     */
    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    /**
     * Check if a specific user is the receiver of this request
     */
    public boolean isReceiver(Long userId) {
        return receiverId.equals(userId);
    }

    /**
     * Check if a specific user is the sender of this request
     */
    public boolean isSender(Long userId) {
        return senderId.equals(userId);
    }
}