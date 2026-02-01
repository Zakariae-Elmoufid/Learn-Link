package org.example.learnlink.modules.matching.entity.enums;

/**
 * Enum representing the status of a user's membership in a group.
 */
public enum MembershipStatus {
    /**
     * User has requested to join (pending approval)
     */
    PENDING,

    /**
     * User is an active member
     */
    ACTIVE,

    /**
     * User has been removed from the group
     */
    REMOVED,

    /**
     * User left the group voluntarily
     */
    LEFT
}
