package org.example.learnlink.modules.matching.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnlink.modules.matching.entity.enums.GroupRole;
import org.example.learnlink.modules.matching.entity.enums.MembershipStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a user's membership in a study group.
 * Tracks role, status, and join date.
 */
@Entity
@Table(name = "group_memberships",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_group_user",
                columnNames = {"group_id", "user_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The study group
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup studyGroup;

    /**
     * ID of the member user
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Role of the user in the group
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GroupRole role = GroupRole.MEMBER;

    /**
     * Status of the membership
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    /**
     * Timestamp when the user left or was removed
     */
    @Column(name = "left_at")
    private LocalDateTime leftAt;

    /**
     * Check if the user has admin privileges (owner or admin)
     */
    public boolean hasAdminPrivileges() {
        return role == GroupRole.OWNER || role == GroupRole.ADMIN;
    }

    /**
     * Check if membership is currently active
     */
    public boolean isActive() {
        return status == MembershipStatus.ACTIVE;
    }
}
