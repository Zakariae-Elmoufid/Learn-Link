package org.example.learnlink.modules.matching.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnlink.modules.matching.entity.enums.GroupStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a study group.
 * Users can create and join groups to collaborate on specific subjects.
 */
@Entity
@Table(name = "study_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the study group
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description of the group's purpose and goals
     */
    @Column(length = 500)
    private String description;

    /**
     * ID of the subject this group focuses on (optional)
     */
    @Column(name = "subject_id")
    private Long subjectId;

    /**
     * ID of the user who created the group
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /**
     * Maximum number of members allowed in the group
     */
    @Column(name = "max_members", nullable = false)
    @Builder.Default
    private Integer maxMembers = 10;

    /**
     * Current status of the group
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVE;

    /**
     * Whether the group is public (anyone can join) or private (requires approval)
     */
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;

    /**
     * Optional cover image URL for the group
     */
    @Column(name = "cover_image_url")
    private String coverImageUrl;

    /**
     * Group memberships
     */
    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupMembership> memberships = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Get the count of active members
     */
    public int getActiveMemberCount() {
        return (int) memberships.stream()
                .filter(m -> m.getStatus() == org.example.learnlink.modules.matching.entity.enums.MembershipStatus.ACTIVE)
                .count();
    }

    /**
     * Check if the group can accept new members
     */
    public boolean canAcceptMembers() {
        return status == GroupStatus.ACTIVE && getActiveMemberCount() < maxMembers;
    }

    /**
     * Check if the group is full
     */
    public boolean isFull() {
        return getActiveMemberCount() >= maxMembers;
    }
}
