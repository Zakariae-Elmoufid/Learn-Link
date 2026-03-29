package org.example.learnlink.modules.matching.service;

import org.example.learnlink.modules.matching.dto.request.SendConnectionRequestDto;
import org.example.learnlink.modules.matching.dto.response.ConnectionRequestResponse;
import org.example.learnlink.modules.matching.dto.response.ConnectionResponse;

import java.util.List;

/**
 * Service interface for managing connections between users.
 * Handles connection requests (send, accept, reject) and established connections.
 */
public interface IConnectionService {

    // ==================== Connection Requests ====================

    /**
     * Send a connection request to another user.
     * Calculates compatibility score and creates a pending request.
     *
     * @param senderId the ID of the user sending the request
     * @param dto      the request details (receiver ID and optional message)
     * @return the created connection request
     * @throws IllegalStateException if users are already connected or request exists
     */
    ConnectionRequestResponse sendConnectionRequest(Long senderId, SendConnectionRequestDto dto);

    /**
     * Accept a pending connection request.
     * Creates a new connection between the two users.
     *
     * @param userId    the ID of the user accepting (must be the receiver)
     * @param requestId the ID of the request to accept
     * @return the newly created connection
     * @throws IllegalStateException if the user is not the receiver
     */
    ConnectionResponse acceptRequest(Long userId, Long requestId);

    /**
     * Reject a pending connection request.
     *
     * @param userId    the ID of the user rejecting (must be the receiver)
     * @param requestId the ID of the request to reject
     * @throws IllegalStateException if the user is not the receiver
     */
    void rejectRequest(Long userId, Long requestId);

    /**
     * Cancel a sent connection request.
     *
     * @param userId    the ID of the user cancelling (must be the sender)
     * @param requestId the ID of the request to cancel
     * @throws IllegalStateException if the user is not the sender
     */
    void cancelRequest(Long userId, Long requestId);

    /**
     * Get all pending requests received by a user.
     *
     * @param userId the ID of the user
     * @return list of pending connection requests
     */
    List<ConnectionRequestResponse> getPendingRequests(Long userId);

    /**
     * Get all requests sent by a user.
     *
     * @param userId the ID of the user
     * @return list of sent connection requests
     */
    List<ConnectionRequestResponse> getSentRequests(Long userId);

    /**
     * Get the count of pending requests for a user (for notification badge).
     *
     * @param userId the ID of the user
     * @return count of pending requests
     */
    long getPendingRequestsCount(Long userId);

    // ==================== Connections ====================

    /**
     * Get all active connections for a user.
     *
     * @param userId the ID of the user
     * @return list of active connections with user details
     */
    List<ConnectionResponse> getActiveConnections(Long userId);

    /**
     * Get a specific connection by ID.
     *
     * @param userId       the ID of the user requesting
     * @param connectionId the ID of the connection
     * @return the connection details
     */
    ConnectionResponse getConnectionById(Long userId, Long connectionId);

    /**
     * Remove/delete a connection.
     * The connection can be removed by either user.
     *
     * @param userId       the ID of the user removing the connection
     * @param connectionId the ID of the connection to remove
     */
    void removeConnection(Long userId, Long connectionId);

    /**
     * Check if two users are connected.
     *
     * @param user1Id first user ID
     * @param user2Id second user ID
     * @return true if they have an active connection
     */
    boolean areConnected(Long user1Id, Long user2Id);

    /**
     * Get the count of active connections for a user.
     *
     * @param userId the ID of the user
     * @return count of active connections
     */
    long getConnectionsCount(Long userId);

    /**
     * Get IDs of all users connected to a specific user.
     *
     * @param userId the ID of the user
     * @return list of connected user IDs
     */
    List<Long> getConnectedUserIds(Long userId);
}
