package org.example.learnlink.modules.planner.repository;

import org.example.learnlink.modules.planner.entity.Task;
import org.example.learnlink.modules.planner.entity.TaskPriority;
import org.example.learnlink.modules.planner.entity.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TaskRepository using H2 in-memory database
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Task Repository Integration Tests")
class TaskRepositoryIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        
        sampleTask = Task.builder()
                .userId(1L)
                .title("Study Spring Boot")
                .description("Complete chapter on integration testing")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(3))
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.PENDING)
                .subject("Programming")
                .completed(false)
                .build();
    }

    @Test
    @DisplayName("Should save and retrieve a task")
    void shouldSaveAndRetrieveTask() {
        // Act
        Task savedTask = taskRepository.save(sampleTask);
        Optional<Task> retrievedTask = taskRepository.findById(savedTask.getId());

        // Assert
        assertThat(retrievedTask).isPresent();
        assertThat(retrievedTask.get().getTitle()).isEqualTo("Study Spring Boot");
        assertThat(retrievedTask.get().getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(retrievedTask.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should find tasks by user ID")
    void shouldFindTasksByUserId() {
        // Arrange
        Task task1 = Task.builder()
                .userId(1L)
                .title("Task 1")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .priority(TaskPriority.LOW)
                .status(TaskStatus.PENDING)
                .completed(false)
                .build();

        Task task2 = Task.builder()
                .userId(1L)
                .title("Task 2")
                .startTime(LocalDateTime.now().plusHours(3))
                .endTime(LocalDateTime.now().plusHours(4))
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.PENDING)
                .completed(false)
                .build();

        Task task3 = Task.builder()
                .userId(2L)
                .title("Task for another user")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.PENDING)
                .completed(false)
                .build();

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        // Act
        List<Task> userTasks = taskRepository.findByUserId(1L);

        // Assert
        assertThat(userTasks).hasSize(2);
        assertThat(userTasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    @DisplayName("Should update task status")
    void shouldUpdateTaskStatus() {
        // Arrange
        Task savedTask = taskRepository.save(sampleTask);

        // Act
        savedTask.setStatus(TaskStatus.IN_PROGRESS);
        Task updatedTask = taskRepository.save(savedTask);

        // Assert
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should delete a task")
    void shouldDeleteTask() {
        // Arrange
        Task savedTask = taskRepository.save(sampleTask);
        Long taskId = savedTask.getId();

        // Act
        taskRepository.deleteById(taskId);
        Optional<Task> deletedTask = taskRepository.findById(taskId);

        // Assert
        assertThat(deletedTask).isEmpty();
    }

    @Test
    @DisplayName("Should find tasks by status")
    void shouldFindTasksByStatus() {
        // Arrange
        Task pendingTask = Task.builder()
                .userId(1L)
                .title("Pending Task")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.PENDING)
                .completed(false)
                .build();

        Task inProgressTask = Task.builder()
                .userId(1L)
                .title("In Progress Task")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .priority(TaskPriority.LOW)
                .status(TaskStatus.IN_PROGRESS)
                .completed(false)
                .build();

        taskRepository.save(pendingTask);
        taskRepository.save(inProgressTask);

        // Act
        List<Task> pendingTasks = taskRepository.findByUserIdAndStatus(1L, TaskStatus.PENDING);

        // Assert
        assertThat(pendingTasks).hasSize(1);
        assertThat(pendingTasks.get(0).getTitle()).isEqualTo("Pending Task");
    }
}
