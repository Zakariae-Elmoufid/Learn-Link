package org.example.learnlink.modules.planner.service;

import org.example.learnlink.modules.planner.dto.TaskRequest;
import org.example.learnlink.modules.planner.dto.TaskResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for task management operations
 * Defines the contract for task-related business logic
 */
public interface ITaskService {

    /**
     * Create a new task for a user
     * 
     * @param userId  User ID
     * @param request Task creation request
     * @return Created task response
     */
    TaskResponse createTask(Long userId, TaskRequest request);

    /**
     * Get a task by its ID
     * 
     * @param taskId Task ID
     * @return Task response
     * @throws org.example.learnlink.common.exception.ResourceNotFoundException if
     *                                                                          task
     *                                                                          not
     *                                                                          found
     */
    TaskResponse getTaskById(Long taskId);

    /**
     * Get all tasks for a user
     * 
     * @param userId User ID
     * @return List of task responses
     */
    List<TaskResponse> getUserTasks(Long userId);

    /**
     * Get active tasks for a user (not completed or cancelled)
     * 
     * @param userId User ID
     * @return List of active task responses
     */
    List<TaskResponse> getActiveTasks(Long userId);

    /**
     * Get tasks within a specific date range
     * 
     * @param userId    User ID
     * @param startTime Start time
     * @param endTime   End time
     * @return List of task responses in the date range
     */
    List<TaskResponse> getTasksByDateRange(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Get today's tasks for a user
     * 
     * @param userId User ID
     * @return List of today's task responses
     */
    List<TaskResponse> getTodayTasks(Long userId);

    /**
     * Get overdue tasks for a user
     * 
     * @param userId User ID
     * @return List of overdue task responses
     */
    List<TaskResponse> getOverdueTasks(Long userId);

    /**
     * Update an existing task
     * 
     * @param taskId  Task ID
     * @param request Task update request
     * @return Updated task response
     * @throws org.example.learnlink.common.exception.ResourceNotFoundException if
     *                                                                          task
     *                                                                          not
     *                                                                          found
     */
    TaskResponse updateTask(Long taskId, TaskRequest request);

    /**
     * Mark a task as completed
     * 
     * @param taskId Task ID
     * @return Completed task response
     * @throws org.example.learnlink.common.exception.ResourceNotFoundException if
     *                                                                          task
     *                                                                          not
     *                                                                          found
     */
    TaskResponse completeTask(Long taskId);

    /**
     * Delete a task
     * 
     * @param taskId Task ID
     * @throws org.example.learnlink.common.exception.ResourceNotFoundException if
     *                                                                          task
     *                                                                          not
     *                                                                          found
     */
    void deleteTask(Long taskId);
}
