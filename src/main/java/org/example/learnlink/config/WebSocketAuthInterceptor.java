package org.example.learnlink.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.auth.security.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * Interceptor for WebSocket authentication using JWT tokens.
 * 
 * Extracts and validates JWT token from Authorization header during STOMP CONNECT.
 * Creates a Principal for the session based on the userId claim in the token.
 * This allows using @MessageMapping with Principal parameter and
 * enables user-specific message routing via /user/{userId}/queue/*.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract JWT token from Authorization header during CONNECT
            String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
            
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());
                
                try {
                    // Extract userId from JWT token
                    Long userId = jwtService.extractUserId(token);
                    
                    if (userId != null) {
                        // Create a Principal for the user
                        Principal principal = new WebSocketPrincipal(userId);
                        accessor.setUser(principal);
                        
                        log.info("WebSocket user connected via JWT: {}", userId);
                    } else {
                        log.warn("WebSocket connection with JWT token missing userId claim");
                    }
                } catch (Exception e) {
                    log.error("WebSocket JWT authentication failed: {}", e.getMessage());
                }
            } else {
                log.warn("WebSocket connection without valid Authorization header");
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
