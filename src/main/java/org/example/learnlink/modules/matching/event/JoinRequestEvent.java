package org.example.learnlink.modules.matching.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when a user requests to join a private group.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequestEvent {

    private Long groupId;

    private String groupName;

    private Long requesterId;

    private Long ownerId;
}
