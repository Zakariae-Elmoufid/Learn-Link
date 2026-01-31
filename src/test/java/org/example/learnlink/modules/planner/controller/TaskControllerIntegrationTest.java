package org.example.learnlink.modules.planner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.learnlink.AbstractIntegrationTest;
import org.example.learnlink.modules.planner.dto.TaskRequest;
import org.example.learnlink.modules.planner.entity.TaskPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Task Controller Integration Tests")
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create and retrieve a task")
    void shouldCreateAndRetrieveTask() throws Exception {
        // Arrange
        TaskRequest request = TaskRequest.builder()
                .title("Integration Test Task")
                .description("Testing with Testcontainers")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .priority(TaskPriority.HIGH)
                .subject("DevOps")
                .tags(Arrays.asList("testing", "docker"))
                .build();

        // Act & Assert - Create Task
        MvcResult createResult = mockMvc.perform(post("/api/planner/tasks")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(request.getTitle()))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        // Extract ID
        String responseContent = createResult.getResponse().getContentAsString();
        Long taskId = objectMapper.readTree(responseContent).get("id").asLong();

        // Act & Assert - Get Task
        mockMvc.perform(get("/api/planner/tasks/" + taskId)
                .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value(request.getTitle()));
    }
}
