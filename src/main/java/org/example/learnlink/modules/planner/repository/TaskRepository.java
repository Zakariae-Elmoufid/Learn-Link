package org.example.learnlink.modules.planner.repository;

import org.example.learnlink.modules.planner.entity.Task;
import org.example.learnlink.modules.planner.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Task operations
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all tasks for a specific user
     */
    List<Task> findByUserId(Long userId);

    /**
     * Find tasks by user and status
     */
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);

    /**
     * Find tasks for a user within a time range
     */
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.startTime >= :startTime AND t.endTime <= :endTime ORDER BY t.startTime ASC")
    List<Task> findByUserIdAndTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find active tasks for a user (not completed or cancelled)
     */
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY t.priority DESC, t.startTime ASC")
    List<Task> findActiveTasksByUserId(@Param("userId") Long userId);

    /**
     * Find overdue tasks for a user
     */
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.completed = false AND t.endTime < :now ORDER BY t.endTime ASC")
    List<Task> findOverdueTasksByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Find completed tasks for a user
     */
    List<Task> findByUserIdAndCompletedTrue(Long userId);

    /**
     * Find tasks by subject
     */
    List<Task> findByUserIdAndSubject(Long userId, String subject);

    /**
     * Count tasks by user
     */
    long countByUserId(Long userId);

    /**
     * Count completed tasks by user
     */
    long countByUserIdAndCompletedTrue(Long userId);

    /**
     * Find today's tasks for a user
     */
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.startTime >= :dayStart AND t.startTime < :dayEnd ORDER BY t.startTime ASC")
    List<Task> findTodayTasks(
            @Param("userId") Long userId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd);
    
    // Admin statistics queries
    
    /**
     * Count all completed tasks
     */
    long countByCompletedTrue();
}
