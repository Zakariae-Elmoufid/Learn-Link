package org.example.learnlink.common.service;

import java.util.List;

/**
 * Service interface for validating group operations.
 * This interface allows the messaging module to validate group membership
 * without directly depending on the matching module.
 */
public interface GroupValidationService {

    /**
     * Check if a group exists.
     *
     * @param groupId the ID of the group
     * @return true if the group exists, false otherwise
     */
    boolean groupExists(Long groupId);

    /**
     * Validate that a user is an active member of a group.
     *
     * @param groupId the ID of the group
     * @param userId the ID of the user
     * @return true if the user is an active member, false otherwise
     */
    boolean isActiveMember(Long groupId, Long userId);

    /**
     * Get the IDs of all active members in a group.
     *
     * @param groupId the ID of the group
     * @return list of user IDs who are active members
     */
    List<Long> getActiveMemberIds(Long groupId);
}
