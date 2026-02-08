package org.example.learnlink.modules.planner.service;

import org.example.learnlink.modules.planner.dto.TaskRequest;
import org.example.learnlink.modules.planner.dto.TaskResponse;
import org.example.learnlink.modules.planner.entity.TaskPriority;
import org.example.learnlink.modules.planner.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TaskService using H2 in-memory database
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Task Service Integration Tests")
class TaskServiceIntegrationTest {

    @Autowired
    private ITaskService taskService;


    @Autowired
    private TaskRepository taskRepository;


    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create a task through the service layer")
    void shouldCreateTaskThroughService() {
        // Arrange
        Long userId = 1L;
        TaskRequest request = TaskRequest.builder()
                .title("Study for exam")
                .description("Prepare for math final")
                .startTime(LocalDateTime.of(2026, 2, 15, 10, 0))
                .endTime(LocalDateTime.of(2026, 2, 15, 13, 0))
                .priority(TaskPriority.HIGH)
                .subject("Mathematics")
                .tags(Arrays.asList("exam", "math"))
                .build();

        // Act
        TaskResponse response = taskService.createTask(userId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Study for exam");
        assertThat(response.getDescription()).isEqualTo("Prepare for math final");
        assertThat(response.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.getSubject()).isEqualTo("Mathematics");
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should retrieve a task by ID through the service layer")
    void shouldRetrieveTaskById() {
        // Arrange
        Long userId = 1L;
        TaskRequest request = TaskRequest.builder()
                .title("Read chapter 5")
                .startTime(LocalDateTime.of(2026, 2, 10, 14, 0))
                .endTime(LocalDateTime.of(2026, 2, 10, 15, 0))
                .priority(TaskPriority.MEDIUM)
                .build();

        TaskResponse createdTask = taskService.createTask(userId, request);

        // Act
        TaskResponse retrievedTask = taskService.getTaskById(createdTask.getId());

        // Assert
        assertThat(retrievedTask).isNotNull();
        assertThat(retrievedTask.getId()).isEqualTo(createdTask.getId());
        assertThat(retrievedTask.getTitle()).isEqualTo("Read chapter 5");
        assertThat(retrievedTask.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(retrievedTask.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should get all user tasks through the service layer")
    void shouldGetAllUserTasks() {
        // Arrange
        Long userId = 1L;

        TaskRequest task1 = TaskRequest.builder()
                .title("Task 1")
                .startTime(LocalDateTime.of(2026, 2, 11, 10, 0))
                .endTime(LocalDateTime.of(2026, 2, 11, 11, 0))
                .priority(TaskPriority.LOW)
                .build();

        TaskRequest task2 = TaskRequest.builder()
                .title("Task 2")
                .startTime(LocalDateTime.of(2026, 2, 11, 14, 0))
                .endTime(LocalDateTime.of(2026, 2, 11, 15, 0))
                .priority(TaskPriority.HIGH)
                .build();

        taskService.createTask(userId, task1);
        taskService.createTask(userId, task2);

        // Act
        List<TaskResponse> userTasks = taskService.getUserTasks(userId);

        // Assert
        assertThat(userTasks).hasSize(2);
        assertThat(userTasks).extracting(TaskResponse::getTitle)
                .containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    @DisplayName("Should get active tasks only")
    void shouldGetActiveTasks() {
        // Arrange
        Long userId = 1L;

        TaskRequest activeTask = TaskRequest.builder()
                .title("Active Task")
                .startTime(LocalDateTime.of(2026, 2, 12, 9, 0))
                .endTime(LocalDateTime.of(2026, 2, 12, 10, 0))
                .priority(TaskPriority.MEDIUM)
                .build();

        taskService.createTask(userId, activeTask);

        // Act
        List<TaskResponse> activeTasks = taskService.getActiveTasks(userId);

        // Assert
        assertThat(activeTasks).isNotEmpty();
        assertThat(activeTasks).hasSize(1);
        assertThat(activeTasks.get(0).getTitle()).isEqualTo("Active Task");
        assertThat(activeTasks.get(0).getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(activeTasks.get(0).getUserId()).isEqualTo(userId);
    }
}
