package org.example.learnlink.modules.planner.service;

import org.example.learnlink.common.exception.ResourceNotFoundException;
import org.example.learnlink.modules.planner.dto.TaskRequest;
import org.example.learnlink.modules.planner.dto.TaskResponse;
import org.example.learnlink.modules.planner.entity.Task;
import org.example.learnlink.modules.planner.entity.TaskPriority;
import org.example.learnlink.modules.planner.entity.TaskStatus;
import org.example.learnlink.modules.planner.event.TaskCompletedEvent;
import org.example.learnlink.modules.planner.event.TaskCreatedEvent;
import org.example.learnlink.modules.planner.mapper.TaskMapper;
import org.example.learnlink.modules.planner.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskServiceImpl
 * 
 * TESTING LIFECYCLE:
 * 1. @BeforeEach - Runs before EACH test method (setup)
 * 2. @Test - The actual test method
 * 3. Assertions verify expected outcomes
 * 
 * MOCKITO USAGE:
 * - @Mock: Creates mock objects (fake dependencies)
 * - @InjectMocks: Creates instance with mocked dependencies injected
 * - when().thenReturn(): Stub method behavior
 * - verify(): Ensure method was called
 * - ArgumentCaptor: Capture arguments passed to methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    // Test data
    private Long userId;
    private Long taskId;
    private Task task;
    private TaskRequest taskRequest;
    private TaskResponse taskResponse;

    /**
     * LIFECYCLE: @BeforeEach runs before EACH test
     * Purpose: Initialize test data to ensure clean state
     */
    @BeforeEach
    void setUp() {
        // Initialize test data
        userId = 1L;
        taskId = 100L;

        // Create test task entity
        task = Task.builder()
                .id(taskId)
                .userId(userId)
                .title("Study Java")
                .description("Study Spring Boot framework")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(3))
                .priority(TaskPriority.HIGH)
                .subject("Computer Science")
                .tags(new ArrayList<>(Arrays.asList("Java", "Spring")))
                .build();

        // Create test request DTO
        taskRequest = TaskRequest.builder()
                .title("Study Java")
                .description("Study Spring Boot framework")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(3))
                .priority(TaskPriority.HIGH)
                .subject("Computer Science")
                .tags(Arrays.asList("Java", "Spring"))
                .build();

        // Create test response DTO
        taskResponse = TaskResponse.builder()
                .id(taskId)
                .userId(userId)
                .title("Study Java")
                .description("Study Spring Boot framework")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.PENDING)
                .build();
    }

    // ==================== CREATE TASK TESTS ====================

    @Test
    @DisplayName("Should create task successfully and publish event")
    void testCreateTask_Success() {
        // ARRANGE (Given) - Set up test conditions
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // ACT (When) - Execute the method being tested
        TaskResponse result = taskService.createTask(userId, taskRequest);

        // ASSERT (Then) - Verify the results
        assertNotNull(result);
        assertEquals(taskResponse.getTitle(), result.getTitle());
        assertEquals(taskResponse.getUserId(), result.getUserId());

        // Verify repository save was called
        verify(taskRepository, times(1)).save(any(Task.class));

        // Verify event was published
        ArgumentCaptor<TaskCreatedEvent> eventCaptor = ArgumentCaptor.forClass(TaskCreatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        TaskCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.getUserId());
        assertEquals(taskId, capturedEvent.getTaskId());
    }

    @Test
    @DisplayName("Should create task with null tags as empty list")
    void testCreateTask_WithNullTags() {
        // Arrange
        TaskRequest requestWithNullTags = TaskRequest.builder()
                .title("Study Java")
                .description("Test description")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .priority(TaskPriority.MEDIUM)
                .tags(null) // Null tags
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);

        // Act
        TaskResponse result = taskService.createTask(userId, requestWithNullTags);

        // Assert
        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
    }

    // ==================== GET TASK TESTS ====================

    @Test
    @DisplayName("Should get task by ID successfully")
    void testGetTaskById_Success() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // Act
        TaskResponse result = taskService.getTaskById(taskId);

        // Assert
        assertNotNull(result);
        assertEquals(taskResponse.getId(), result.getId());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskMapper, times(1)).toResponse(task);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when task not found")
    void testGetTaskById_NotFound() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTaskById(taskId));

        assertTrue(exception.getMessage().contains("Task"));
        assertTrue(exception.getMessage().contains("id"));
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskMapper, never()).toResponse(any());
    }

    // ==================== GET USER TASKS TESTS ====================

    @Test
    @DisplayName("Should get all tasks for user")
    void testGetUserTasks_Success() {
        // Arrange
        List<Task> tasks = Arrays.asList(task, task);
        List<TaskResponse> responses = Arrays.asList(taskResponse, taskResponse);

        when(taskRepository.findByUserId(userId)).thenReturn(tasks);
        when(taskMapper.toResponseList(tasks)).thenReturn(responses);

        // Act
        List<TaskResponse> result = taskService.getUserTasks(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findByUserId(userId);
        verify(taskMapper, times(1)).toResponseList(tasks);
    }

    @Test
    @DisplayName("Should return empty list when user has no tasks")
    void testGetUserTasks_Empty() {
        // Arrange
        when(taskRepository.findByUserId(userId)).thenReturn(new ArrayList<>());
        when(taskMapper.toResponseList(anyList())).thenReturn(new ArrayList<>());

        // Act
        List<TaskResponse> result = taskService.getUserTasks(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).findByUserId(userId);
    }

    // ==================== GET ACTIVE TASKS TESTS ====================

    @Test
    @DisplayName("Should get active tasks for user")
    void testGetActiveTasks_Success() {
        // Arrange
        List<Task> activeTasks = Arrays.asList(task);
        List<TaskResponse> responses = Arrays.asList(taskResponse);

        when(taskRepository.findActiveTasksByUserId(userId)).thenReturn(activeTasks);
        when(taskMapper.toResponseList(activeTasks)).thenReturn(responses);

        // Act
        List<TaskResponse> result = taskService.getActiveTasks(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findActiveTasksByUserId(userId);
    }

    // ==================== DATE RANGE TESTS ====================

    @Test
    @DisplayName("Should get tasks by date range")
    void testGetTasksByDateRange_Success() {
        // Arrange
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        List<Task> tasks = Arrays.asList(task);
        List<TaskResponse> responses = Arrays.asList(taskResponse);

        when(taskRepository.findByUserIdAndTimeRange(userId, start, end)).thenReturn(tasks);
        when(taskMapper.toResponseList(tasks)).thenReturn(responses);

        // Act
        List<TaskResponse> result = taskService.getTasksByDateRange(userId, start, end);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByUserIdAndTimeRange(userId, start, end);
    }

    // ==================== TODAY'S TASKS TESTS ====================

    @Test
    @DisplayName("Should get today's tasks")
    void testGetTodayTasks_Success() {
        // Arrange
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(LocalTime.MAX);
        List<Task> tasks = Arrays.asList(task);
        List<TaskResponse> responses = Arrays.asList(taskResponse);

        when(taskRepository.findTodayTasks(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(tasks);
        when(taskMapper.toResponseList(tasks)).thenReturn(responses);

        // Act
        List<TaskResponse> result = taskService.getTodayTasks(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1))
                .findTodayTasks(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    // ==================== OVERDUE TASKS TESTS ====================

    @Test
    @DisplayName("Should get overdue tasks")
    void testGetOverdueTasks_Success() {
        // Arrange
        List<Task> overdueTasks = Arrays.asList(task);
        List<TaskResponse> responses = Arrays.asList(taskResponse);

        when(taskRepository.findOverdueTasksByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(overdueTasks);
        when(taskMapper.toResponseList(overdueTasks)).thenReturn(responses);

        // Act
        List<TaskResponse> result = taskService.getOverdueTasks(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1))
                .findOverdueTasksByUserId(eq(userId), any(LocalDateTime.class));
    }

    // ==================== UPDATE TASK TESTS ====================

    @Test
    @DisplayName("Should update task successfully")
    void testUpdateTask_Success() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // Act
        TaskResponse result = taskService.updateTask(taskId, taskRequest);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(task);
        verify(taskMapper, times(1)).toResponse(task);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent task")
    void testUpdateTask_NotFound() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskService.updateTask(taskId, taskRequest));
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any());
    }

    // ==================== COMPLETE TASK TESTS ====================

    @Test
    @DisplayName("Should complete task and publish event")
    void testCompleteTask_Success() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // Act
        TaskResponse result = taskService.completeTask(taskId);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(task);

        // Verify completion event was published
        ArgumentCaptor<TaskCompletedEvent> eventCaptor = ArgumentCaptor.forClass(TaskCompletedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        TaskCompletedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(taskId, capturedEvent.getTaskId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when completing non-existent task")
    void testCompleteTask_NotFound() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskService.completeTask(taskId));
        verify(taskRepository, times(1)).findById(taskId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ==================== DELETE TASK TESTS ====================

    @Test
    @DisplayName("Should delete task successfully")
    void testDeleteTask_Success() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(task);

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent task")
    void testDeleteTask_NotFound() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskService.deleteTask(taskId));
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).delete(any());
    }
}
