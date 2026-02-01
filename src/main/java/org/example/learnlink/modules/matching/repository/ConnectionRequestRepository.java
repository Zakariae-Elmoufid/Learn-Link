package org.example.learnlink.modules.matching.repository;

import org.example.learnlink.modules.matching.entity.ConnectionRequest;
import org.example.learnlink.modules.matching.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing connection requests between users
 */
@Repository
public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Long> {

    /**
     * Find all pending requests received by a user
     *
     * @param receiverId the ID of the user who received the requests
     * @param status     the status to filter by (typically PENDING)
     * @return list of connection requests
     */
    List<ConnectionRequest> findByReceiverIdAndStatus(Long receiverId, RequestStatus status);

    /**
     * Find all requests sent by a user
     *
     * @param senderId the ID of the user who sent the requests
     * @return list of connection requests
     */
    List<ConnectionRequest> findBySenderId(Long senderId);

    /**
     * Find all requests sent by a user with a specific status
     *
     * @param senderId the ID of the sender
     * @param status   the status to filter by
     * @return list of connection requests
     */
    List<ConnectionRequest> findBySenderIdAndStatus(Long senderId, RequestStatus status);

    /**
     * Check if a connection request already exists between two users (in either direction)
     *
     * @param user1 first user ID
     * @param user2 second user ID
     * @return the existing request if found
     */
    @Query("SELECT cr FROM ConnectionRequest cr WHERE " +
            "(cr.senderId = :user1 AND cr.receiverId = :user2) OR " +
            "(cr.senderId = :user2 AND cr.receiverId = :user1)")
    Optional<ConnectionRequest> findBetweenUsers(@Param("user1") Long user1, @Param("user2") Long user2);

    /**
     * Check if a pending request exists between two users
     *
     * @param user1 first user ID
     * @param user2 second user ID
     * @return the existing pending request if found
     */
    @Query("SELECT cr FROM ConnectionRequest cr WHERE " +
            "((cr.senderId = :user1 AND cr.receiverId = :user2) OR " +
            "(cr.senderId = :user2 AND cr.receiverId = :user1)) " +
            "AND cr.status = 'PENDING'")
    Optional<ConnectionRequest> findPendingBetweenUsers(@Param("user1") Long user1, @Param("user2") Long user2);

    /**
     * Count pending requests for a user (for notification badge)
     *
     * @param receiverId the user ID
     * @param status     the status to count
     * @return count of requests
     */
    long countByReceiverIdAndStatus(Long receiverId, RequestStatus status);

    /**
     * Check if a request exists from sender to receiver
     *
     * @param senderId   the sender ID
     * @param receiverId the receiver ID
     * @return true if exists
     */
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
}
