package org.example.learnlink.modules.matching.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnlink.modules.matching.entity.enums.ConnectionStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an established connection between two users.
 * Created when a connection request is accepted.
 * This is a bidirectional relationship - both users are connected to each other.
 */
@Entity
@Table(name = "connections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_connection_users",
                columnNames = {"user1_id", "user2_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * First user in the connection (typically the one who sent the request)
     */
    @Column(name = "user1_id", nullable = false)
    private Long user1Id;

    /**
     * Second user in the connection (typically the one who accepted the request)
     */
    @Column(name = "user2_id", nullable = false)
    private Long user2Id;

    /**
     * Current status of the connection (ACTIVE, BLOCKED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConnectionStatus status = ConnectionStatus.ACTIVE;

    /**
     * Compatibility score between the two users (0-100)
     * Copied from the connection request when the connection is established
     */
    @Column(name = "compatibility_score", precision = 5, scale = 2)
    private BigDecimal compatibilityScore;

    /**
     * Timestamp when the connection was established
     */
    @CreationTimestamp
    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;

    /**
     * Check if this connection involves a specific user
     */
    public boolean involvesUser(Long userId) {
        return user1Id.equals(userId) || user2Id.equals(userId);
    }

    /**
     * Get the other user's ID in this connection
     */
    public Long getOtherUserId(Long userId) {
        if (user1Id.equals(userId)) {
            return user2Id;
        } else if (user2Id.equals(userId)) {
            return user1Id;
        }
        throw new IllegalArgumentException("User " + userId + " is not part of this connection");
    }

    /**
     * Check if the connection is currently active
     */
    public boolean isActive() {
        return status == ConnectionStatus.ACTIVE;
    }
}
