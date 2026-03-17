package org.example.learnlink.modules.notification.event;

import lombok.Getter;
import org.example.learnlink.modules.notification.entity.NotificationType;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * Generic event for triggering notifications.
 * Published by various modules, consumed by NotificationEventListener.
 */
@Getter
public class NotificationEvent extends ApplicationEvent {

    private final Long userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final Map<String, Object> data;

    public NotificationEvent(Object source, Long userId, NotificationType type,
                             String title, String message, Map<String, Object> data) {
        super(source);
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.data = data;
    }

    public static NotificationEventBuilder builder(Object source) {
        return new NotificationEventBuilder(source);
    }

    public static class NotificationEventBuilder {
        private final Object source;
        private Long userId;
        private NotificationType type;
        private String title;
        private String message;
        private Map<String, Object> data;

        public NotificationEventBuilder(Object source) {
            this.source = source;
        }

        public NotificationEventBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public NotificationEventBuilder type(NotificationType type) {
            this.type = type;
            return this;
        }

        public NotificationEventBuilder title(String title) {
            this.title = title;
            return this;
        }

        public NotificationEventBuilder message(String message) {
            this.message = message;
            return this;
        }

        public NotificationEventBuilder data(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public NotificationEvent build() {
            return new NotificationEvent(source, userId, type, title, message, data);
        }
    }
}
