package org.example.learnlink.modules.messaging.service;

import java.util.Set;

/**
 * Service for managing user presence/online status.
 */
public interface PresenceService {

    /**
     * Mark user as online
     */
    void setOnline(Long userId);

    /**
     * Mark user as offline
     */
    void setOffline(Long userId);

    /**
     * Check if user is online
     */
    boolean isOnline(Long userId);

    /**
     * Get all online users
     */
    Set<Long> getOnlineUsers();

    /**
     * Get online users from a list
     */
    Set<Long> getOnlineUsers(Set<Long> userIds);
}
