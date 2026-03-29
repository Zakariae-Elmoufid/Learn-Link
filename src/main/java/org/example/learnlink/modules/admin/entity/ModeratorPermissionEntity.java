package org.example.learnlink.modules.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity to store moderator permissions.
 * Links a user with MODERATOR role to their specific permissions.
 */
@Entity
@Table(name = "moderator_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeratorPermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "moderator_permission_list",
            joinColumns = @JoinColumn(name = "moderator_permission_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    @Builder.Default
    private Set<ModeratorPermission> permissions = new HashSet<>();

    @Column(name = "assigned_by", nullable = false)
    private Long assignedBy;  // Admin who granted moderator role

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "notes", length = 500)
    private String notes;  // Admin notes about this moderator

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
