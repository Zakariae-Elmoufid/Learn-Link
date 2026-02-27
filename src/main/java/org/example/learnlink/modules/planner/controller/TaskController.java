package org.example.learnlink.modules.planner.controller;

import org.example.learnlink.modules.auth.security.CustomUserDetails;
import org.example.learnlink.modules.planner.dto.TaskRequest;
import org.example.learnlink.modules.planner.dto.TaskResponse;
import org.example.learnlink.modules.planner.service.ITaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Task management
 */
@RestController
@RequestMapping("/api/planner/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final ITaskService taskService;

    /**
     * Create a new task
     * POST /api/planner/tasks
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TaskRequest request) {
        Long userId = userDetails.getId();
        TaskResponse response = taskService.createTask(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get task by ID
     * GET /api/planner/tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long taskId) {
        TaskResponse response = taskService.getTaskById(taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all tasks for current user
     * GET /api/planner/tasks
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getUserTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<TaskResponse> tasks = taskService.getUserTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get active tasks (not completed or cancelled)
     * GET /api/planner/tasks/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<TaskResponse>> getActiveTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<TaskResponse> tasks = taskService.getActiveTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get today's tasks
     * GET /api/planner/tasks/today
     */
    @GetMapping("/today")
    public ResponseEntity<List<TaskResponse>> getTodayTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<TaskResponse> tasks = taskService.getTodayTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get tasks by date range
     * GET /api/planner/tasks/range?startTime=...&endTime=...
     */
    @GetMapping("/range")
    public ResponseEntity<List<TaskResponse>> getTasksByDateRange(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Long userId = userDetails.getId();
        List<TaskResponse> tasks = taskService.getTasksByDateRange(userId, startTime, endTime);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get overdue tasks
     * GET /api/planner/tasks/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponse>> getOverdueTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<TaskResponse> tasks = taskService.getOverdueTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Update a task
     * PUT /api/planner/tasks/{taskId}
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Complete a task
     * POST /api/planner/tasks/{taskId}/complete
     */
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable Long taskId) {
        TaskResponse response = taskService.completeTask(taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a task
     * DELETE /api/planner/tasks/{taskId}
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
