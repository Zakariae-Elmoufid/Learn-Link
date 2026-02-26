package org.example.learnlink.modules.messaging.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.config.WebSocketAuthInterceptor.WebSocketPrincipal;
import org.example.learnlink.modules.messaging.dto.PresenceUpdate;
import org.example.learnlink.modules.messaging.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * Listener for WebSocket session events.
 * 
 * <h2>Purpose:</h2>
 * Tracks user connections/disconnections for online presence feature.
 * 
 * <h3>Event Flow:</h3>
 * <pre>
 * 1. User connects via WebSocket
 * 2. SessionConnectedEvent fired
 * 3. Mark user as online
 * 4. Broadcast presence update to /topic/presence
 * 
 * 1. User disconnects (close browser, network issue, etc.)
 * 2. SessionDisconnectEvent fired
 * 3. Mark user as offline
 * 4. Broadcast presence update to /topic/presence
 * </pre>
 * 
 * <h3>Client Subscription:</h3>
 * Clients subscribe to /topic/presence to receive online/offline updates.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;

    /**
     * Handle WebSocket connection event.
     */
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();

        if (principal != null) {
            Long userId = getUserId(principal);
            
            // Update presence in Redis/cache
            presenceService.setOnline(userId);
            
            // Broadcast online status to all subscribers
            broadcastPresenceUpdate(userId, true);
            
            log.info("User {} connected via WebSocket", userId);
        }
    }

    /**
     * Handle WebSocket disconnection event.
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();

        if (principal != null) {
            Long userId = getUserId(principal);
            
            // Update presence in Redis/cache
            presenceService.setOffline(userId);
            
            // Broadcast offline status to all subscribers
            broadcastPresenceUpdate(userId, false);
            
            log.info("User {} disconnected from WebSocket", userId);
        }
    }

    /**
     * Broadcast presence update to all connected clients.
     */
    private void broadcastPresenceUpdate(Long userId, boolean online) {
        PresenceUpdate update = PresenceUpdate.builder()
                .userId(userId)
                .online(online)
                .lastSeen(LocalDateTime.now())
                .build();

        // Broadcast to /topic/presence - all subscribers receive this
        messagingTemplate.convertAndSend("/topic/presence", update);
    }

    /**
     * Extract user ID from Principal.
     */
    private Long getUserId(Principal principal) {
        if (principal instanceof WebSocketPrincipal) {
            return ((WebSocketPrincipal) principal).getUserId();
        }
        return Long.parseLong(principal.getName());
    }
}
