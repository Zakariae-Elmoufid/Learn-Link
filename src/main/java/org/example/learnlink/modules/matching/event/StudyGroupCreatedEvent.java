package org.example.learnlink.modules.matching.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when a new study group is created.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupCreatedEvent {

    private Long groupId;

    private String groupName;

    private Long ownerId;

    private Long subjectId;
}
