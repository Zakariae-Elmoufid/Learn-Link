package org.example.learnlink.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for real-time messaging.
 * 
 * <h2>How WebSocket Works in This Application:</h2>
 * 
 * <h3>1. Connection Flow:</h3>
 * <pre>
 * Client                          Server
 *   |                                |
 *   |------ HTTP Upgrade Request --->|  (ws://localhost:8080/ws)
 *   |<----- 101 Switching Protocols -|
 *   |                                |
 *   |====== WebSocket Connection ====|  (Full-duplex)
 *   |                                |
 *   |------ STOMP CONNECT ---------->|
 *   |<----- STOMP CONNECTED ---------|
 * </pre>
 * 
 * <h3>2. Message Flow:</h3>
 * <pre>
 * - /app/*        : Application destination prefix (client -> server)
 * - /topic/*      : Broadcast to all subscribers (server -> clients)
 * - /queue/*      : Point-to-point messaging (server -> specific client)
 * - /user/queue/* : User-specific messages
 * </pre>
 * 
 * <h3>3. STOMP Protocol:</h3>
 * STOMP (Simple Text Oriented Messaging Protocol) provides:
 * - SUBSCRIBE: Client subscribes to a destination
 * - SEND: Client sends message to a destination
 * - MESSAGE: Server sends message to subscribers
 * 
 * <h3>4. Example Client Usage (JavaScript):</h3>
 * <pre>
 * const socket = new SockJS('/ws');
 * const stompClient = Stomp.over(socket);
 * 
 * // Connect with JWT token in Authorization header
 * const token = 'your-jwt-token-here';
 * stompClient.connect({'Authorization': 'Bearer ' + token}, (frame) => {
 *     // Subscribe to personal messages
 *     stompClient.subscribe('/user/queue/messages', (message) => {
 *         console.log('New message:', JSON.parse(message.body));
 *     });
 *     
 *     // Subscribe to typing indicators
 *     stompClient.subscribe('/user/queue/typing', (indicator) => {
 *         console.log('Typing:', JSON.parse(indicator.body));
 *     });
 *     
 *     // Subscribe to online status
 *     stompClient.subscribe('/topic/presence', (status) => {
 *         console.log('Presence update:', JSON.parse(status.body));
 *     });
 * });
 * 
 * // Send a message
 * stompClient.send('/app/chat.send', {}, JSON.stringify({
 *     recipientId: 456,
 *     content: 'Hello!',
 *     type: 'TEXT'
 * }));
 * 
 * // Send typing indicator
 * stompClient.send('/app/chat.typing', {}, JSON.stringify({
 *     recipientId: 456,
 *     typing: true
 * }));
 * </pre>
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    /**
     * Configure the message broker.
     * 
     * - enableSimpleBroker: Enables in-memory message broker for subscriptions
     *   - /topic: For broadcast messages (e.g., presence updates)
     *   - /queue: For point-to-point messages (e.g., private chat)
     * 
     * - setApplicationDestinationPrefixes: Prefix for messages FROM clients
     *   - Messages sent to /app/* are routed to @MessageMapping methods
     * 
     * - setUserDestinationPrefix: Prefix for user-specific destinations
     *   - /user/queue/messages becomes /user/{userId}/queue/messages
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for subscriptions
        // /topic - for broadcast messages to multiple subscribers
        // /queue - for point-to-point messages to specific users
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages from client to server
        // Client sends to /app/chat.send -> handled by @MessageMapping("/chat.send")
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific messages
        // Server sends to /user/{userId}/queue/messages
        // Client subscribes to /user/queue/messages (userId resolved automatically)
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints.
     * 
     * - /ws: WebSocket endpoint for clients to connect
     * - withSockJS(): Fallback for browsers that don't support WebSocket
     * - setAllowedOriginPatterns: CORS configuration
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint - clients connect here
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*")  // Configure for production
                .withSockJS();  // SockJS fallback for older browsers
        
        // Pure WebSocket endpoint (without SockJS)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    /**
     * Configure client inbound channel.
     * Add interceptor for authentication/authorization.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
