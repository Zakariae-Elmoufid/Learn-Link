package org.example.learnlink.modules.gamification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.AddPointsRequest;
import org.example.learnlink.modules.gamification.service.GamificationService;
import org.example.learnlink.modules.gamification.service.UserBadgeService;
import org.example.learnlink.modules.planner.event.TaskCompletedEvent;
import org.example.learnlink.modules.planner.event.TaskCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for planner module gamification events.
 * Awards points for task-related activities.
 * All handlers are async to avoid blocking the main transaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlannerGamificationListener {

    private final GamificationService gamificationService;
    private final UserBadgeService userBadgeService;

    /**
     * Handle task created event - award points for planning
     */
    @Async
    @EventListener
    public void handleTaskCreatedEvent(TaskCreatedEvent event) {
        log.info("Task created event received: userId={}, taskId={}",
                event.getUserId(), event.getTaskId());

        try {
            AddPointsRequest request = AddPointsRequest.builder()
                    .points(2)
                    .actionType("TASK_CREATED")
                    .build();

            gamificationService.addPoints(event.getUserId(), request);
            log.info("Points awarded for task creation: userId={}, points=2",
                    event.getUserId());
        } catch (Exception e) {
            log.error("Error awarding points for task creation: {}", e.getMessage());
        }
    }

    /**
     * Handle task completed event - award points for productivity
     */
    @Async
    @EventListener
    public void handleTaskCompletedEvent(TaskCompletedEvent event) {
        log.info("Task completed event received: userId={}, taskId={}",
                event.getUserId(), event.getTaskId());

        try {
            // Award points based on task priority if available
            int points = 10;

            AddPointsRequest request = AddPointsRequest.builder()
                    .points(points)
                    .actionType("TASK_COMPLETED")
                    .build();

            gamificationService.addPoints(event.getUserId(), request);
            log.info("Points awarded for task completion: userId={}, points={}",
                    event.getUserId(), points);

            // Award PRODUCTIVE_LEARNER badge (ID 3) for completing tasks
            userBadgeService.awardBadgeToUser(event.getUserId(), 3L);
        } catch (Exception e) {
            log.error("Error awarding points for task completion: {}", e.getMessage());
        }
    }
}
