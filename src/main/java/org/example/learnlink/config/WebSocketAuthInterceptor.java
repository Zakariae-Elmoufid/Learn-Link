package org.example.learnlink.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * Interceptor for WebSocket authentication.
 * 
 * Extracts user ID from headers and creates a Principal for the session.
 * This allows using @MessageMapping with Principal parameter and
 * enables user-specific message routing via /user/{userId}/queue/*.
 */
@Component
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract user ID from headers during CONNECT
            String userIdHeader = accessor.getFirstNativeHeader(USER_ID_HEADER);
            
            if (userIdHeader != null) {
                Long userId = Long.parseLong(userIdHeader);
                
                // Create a Principal for the user
                Principal principal = new WebSocketPrincipal(userId);
                accessor.setUser(principal);
                
                log.info("WebSocket user connected: {}", userId);
            } else {
                log.warn("WebSocket connection without X-User-Id header");
            }
        }
        
        return message;
    }

    /**
     * Simple Principal implementation for WebSocket sessions.
     */
    public static class WebSocketPrincipal implements Principal {
        private final Long userId;

        public WebSocketPrincipal(Long userId) {
            this.userId = userId;
        }

        @Override
        public String getName() {
            return userId.toString();
        }

        public Long getUserId() {
            return userId;
        }
    }
}
