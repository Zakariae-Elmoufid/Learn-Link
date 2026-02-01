package org.example.learnlink.modules.matching.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when a user leaves or is removed from a study group.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberLeftGroupEvent {

    private Long groupId;

    private String groupName;

    private Long userId;

    private Long ownerId;

    /**
     * True if user left voluntarily, false if removed by admin
     */
    private boolean voluntary;

    /**
     * ID of the admin who removed the user (null if voluntary)
     */
    private Long removedByUserId;
}
