package org.example.learnlink.modules.planner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.learnlink.modules.planner.dto.TaskRequest;
import org.example.learnlink.modules.planner.dto.TaskResponse;
import org.example.learnlink.modules.planner.entity.TaskPriority;
import org.example.learnlink.modules.planner.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TaskController using H2 in-memory database
 * Tests HTTP endpoints with MockMvc
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DisplayName("Task Controller Integration Tests")
class TaskControllerIntegrationTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        // Build MockMvc from WebApplicationContext without security filters
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
        
        // Create ObjectMapper with JavaTimeModule for LocalDateTime support
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/planner/tasks - Should create a new task")
    void shouldCreateTask() throws Exception {
        // Arrange
        TaskRequest request = TaskRequest.builder()
                .title("Study Java")
                .description("Complete Spring Boot chapter")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(3))
                .priority(TaskPriority.HIGH)
                .subject("Programming")
                .tags(Arrays.asList("java", "spring"))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/planner/tasks")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Study Java"))
                .andExpect(jsonPath("$.description").value("Complete Spring Boot chapter"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.subject").value("Programming"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("GET /api/planner/tasks/{id} - Should retrieve a task by ID")
    void shouldGetTaskById() throws Exception {
        // Arrange - Create a task first
        TaskRequest request = TaskRequest.builder()
                .title("Math Homework")
                .description("Solve calculus problems")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .priority(TaskPriority.MEDIUM)
                .subject("Mathematics")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/planner/tasks")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Act & Assert - Retrieve the task
        mockMvc.perform(get("/api/planner/tasks/" + taskId)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Math Homework"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    @DisplayName("GET /api/planner/tasks - Should get all tasks for a user")
    void shouldGetUserTasks() throws Exception {
        // Arrange - Create multiple tasks
        Long userId = 1L;

        TaskRequest task1 = TaskRequest.builder()
                .title("Task 1")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .priority(TaskPriority.LOW)
                .build();

        TaskRequest task2 = TaskRequest.builder()
                .title("Task 2")
                .startTime(LocalDateTime.now().plusHours(3))
                .endTime(LocalDateTime.now().plusHours(4))
                .priority(TaskPriority.HIGH)
                .build();

        mockMvc.perform(post("/api/planner/tasks")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/planner/tasks")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task2)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/api/planner/tasks")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/planner/tasks - Should return 400 for invalid request")
    void shouldReturn400ForInvalidRequest() throws Exception {
        // Arrange - Missing required fields
        TaskRequest invalidRequest = TaskRequest.builder()
                .title("") // Invalid: empty title
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/planner/tasks")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/planner/tasks/{id} - Should return 404 for non-existent task")
    void shouldReturn404ForNonExistentTask() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/planner/tasks/99999")
                        .header("X-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/planner/tasks/{id} - Should delete a task")
    void shouldDeleteTask() throws Exception {
        // Arrange - Create a task first
        TaskRequest request = TaskRequest.builder()
                .title("Task to delete")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .priority(TaskPriority.LOW)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/planner/tasks")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Act - Delete the task
        mockMvc.perform(delete("/api/planner/tasks/" + taskId)
                        .header("X-User-Id", "1"))
                .andExpect(status().isNoContent());

        // Assert - Task no longer exists
        mockMvc.perform(get("/api/planner/tasks/" + taskId)
                        .header("X-User-Id", "1"))
                .andExpect(status().isNotFound());
    }
}
