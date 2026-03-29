package org.example.learnlink.modules.planner.service;

import org.example.learnlink.modules.planner.dto.TaskRequest;
import org.example.learnlink.modules.planner.dto.TaskResponse;
import org.example.learnlink.modules.planner.entity.Task;
import org.example.learnlink.modules.planner.event.TaskCompletedEvent;
import org.example.learnlink.modules.planner.event.TaskCreatedEvent;
import org.example.learnlink.common.exception.ResourceNotFoundException;
import org.example.learnlink.modules.planner.mapper.TaskMapper;
import org.example.learnlink.modules.planner.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of task management service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements ITaskService {

    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TaskMapper taskMapper; // MapStruct mapper

    @Override
    public TaskResponse createTask(Long userId, TaskRequest request) {
        log.info("Creating task for user: {}", userId);

        Task task = Task.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .priority(request.getPriority())
                .subject(request.getSubject())
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .build();

        Task savedTask = taskRepository.save(task);

        // Publish event for gamification (+5 points)
        eventPublisher.publishEvent(new TaskCreatedEvent(this, userId, savedTask.getId(), savedTask));

        return taskMapper.toResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getUserTasks(Long userId) {
        List<Task> tasks = taskRepository.findByUserId(userId);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getActiveTasks(Long userId) {
        List<Task> tasks = taskRepository.findActiveTasksByUserId(userId);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByDateRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Task> tasks = taskRepository.findByUserIdAndTimeRange(userId, startTime, endTime);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTodayTasks(Long userId) {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(LocalTime.MAX);
        List<Task> tasks = taskRepository.findTodayTasks(userId, dayStart, dayEnd);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(Long userId) {
        List<Task> tasks = taskRepository.findOverdueTasksByUserId(userId, LocalDateTime.now());
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public TaskResponse updateTask(Long taskId, TaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStartTime(request.getStartTime());
        task.setEndTime(request.getEndTime());
        task.setPriority(request.getPriority());
        task.setSubject(request.getSubject());
        if (request.getTags() != null) {
            task.setTags(request.getTags());
        }

        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public TaskResponse completeTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        task.complete();
        Task completedTask = taskRepository.save(task);

        // Publish event for gamification (+10 points)
        eventPublisher.publishEvent(new TaskCompletedEvent(this, task.getUserId(), taskId, completedTask));

        return taskMapper.toResponse(completedTask);
    }

    @Override
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        taskRepository.delete(task);
        log.info("Deleted task: {}", taskId);
    }
}
