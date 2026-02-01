package org.example.learnlink.modules.matching.repository;

import org.example.learnlink.modules.matching.entity.Connection;
import org.example.learnlink.modules.matching.entity.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing established connections between users
 */
@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    /**
     * Find all connections for a user with a specific status
     *
     * @param userId the user ID
     * @param status the connection status (ACTIVE, BLOCKED)
     * @return list of connections
     */
    @Query("SELECT c FROM Connection c WHERE " +
            "(c.user1Id = :userId OR c.user2Id = :userId) AND c.status = :status")
    List<Connection> findByUserIdAndStatus(@Param("userId") Long userId,
                                           @Param("status") ConnectionStatus status);

    /**
     * Find all active connections for a user
     *
     * @param userId the user ID
     * @return list of active connections
     */
    @Query("SELECT c FROM Connection c WHERE " +
            "(c.user1Id = :userId OR c.user2Id = :userId) AND c.status = 'ACTIVE'")
    List<Connection> findActiveByUserId(@Param("userId") Long userId);

    /**
     * Check if two users are connected with a specific status
     *
     * @param user1  first user ID
     * @param user2  second user ID
     * @param status the connection status
     * @return the connection if found
     */
    @Query("SELECT c FROM Connection c WHERE " +
            "((c.user1Id = :user1 AND c.user2Id = :user2) OR " +
            "(c.user1Id = :user2 AND c.user2Id = :user1)) AND c.status = :status")
    Optional<Connection> findBetweenUsersWithStatus(@Param("user1") Long user1,
                                                     @Param("user2") Long user2,
                                                     @Param("status") ConnectionStatus status);

    /**
     * Check if two users are connected (any status)
     *
     * @param user1 first user ID
     * @param user2 second user ID
     * @return the connection if found
     */
    @Query("SELECT c FROM Connection c WHERE " +
            "(c.user1Id = :user1 AND c.user2Id = :user2) OR " +
            "(c.user1Id = :user2 AND c.user2Id = :user1)")
    Optional<Connection> findBetweenUsers(@Param("user1") Long user1, @Param("user2") Long user2);

    /**
     * Check if two users have an active connection
     *
     * @param user1 first user ID
     * @param user2 second user ID
     * @return true if an active connection exists
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Connection c WHERE " +
            "((c.user1Id = :user1 AND c.user2Id = :user2) OR " +
            "(c.user1Id = :user2 AND c.user2Id = :user1)) AND c.status = 'ACTIVE'")
    boolean existsActiveConnectionBetween(@Param("user1") Long user1, @Param("user2") Long user2);

    /**
     * Count active connections for a user
     *
     * @param userId the user ID
     * @return count of active connections
     */
    @Query("SELECT COUNT(c) FROM Connection c WHERE " +
            "(c.user1Id = :userId OR c.user2Id = :userId) AND c.status = 'ACTIVE'")
    long countActiveByUserId(@Param("userId") Long userId);

    /**
     * Get all user IDs connected to a specific user
     *
     * @param userId the user ID
     * @return list of connected user IDs
     */
    @Query("SELECT CASE WHEN c.user1Id = :userId THEN c.user2Id ELSE c.user1Id END " +
            "FROM Connection c WHERE (c.user1Id = :userId OR c.user2Id = :userId) " +
            "AND c.status = 'ACTIVE'")
    List<Long> findConnectedUserIds(@Param("userId") Long userId);
}
