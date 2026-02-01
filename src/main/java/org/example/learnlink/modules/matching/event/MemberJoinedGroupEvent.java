package org.example.learnlink.modules.matching.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when a user joins a study group.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberJoinedGroupEvent {

    private Long groupId;

    private String groupName;

    private Long userId;

    private Long ownerId;

    /**
     * True if this was a direct join (public group), false if via approved request
     */
    private boolean directJoin;
}
