package org.example.learnlink.modules.planner.event;

import org.example.learnlink.modules.planner.entity.Task;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a task is completed
 */
@Getter
public class TaskCompletedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long taskId;
    private final Task task;

    public TaskCompletedEvent(Object source, Long userId, Long taskId, Task task) {
        super(source);
        this.userId = userId;
        this.taskId = taskId;
        this.task = task;
    }
}
