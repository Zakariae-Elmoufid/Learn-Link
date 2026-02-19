# LearnLink - Real-Time Messaging System

A robust real-time messaging platform built with **Spring Boot**, following **Clean Architecture** principles, **modular design**, and **event-driven architecture**.

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-blue.svg)](https://stomp.github.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Table of Contents

- [Functional Features](#1-functional-features)
- [Architecture](#2-architecture)
- [Real-Time Communication](#3-real-time-communication)
- [Event-Driven Design](#4-event-driven-design)
- [Exception Handling](#5-exception-handling)
- [Logging](#6-logging)
- [Database](#7-database)
- [Project Structure](#8-project-structure)
- [Scalability](#9-scalability)
- [Sequence Flow](#10-sequence-flow)
- [Getting Started](#getting-started)

---

## 1. Functional Features

### Core Messaging

| Feature | Description |
|---------|-------------|
| **Chat 1-to-1** | Real-time private messaging between two users with instant delivery |
| **Send Text Message** | Compose and send text messages with support for emojis and formatting |
| **Message History** | Retrieve paginated conversation history with search capabilities |

### File & Interaction

| Feature | Description |
|---------|-------------|
| **Attach File** | Share documents, images, and media files up to configurable size limits |
| **Typing Indicator** | Real-time "user is typing..." notifications |
| **Read Receipt (✓✓)** | Double-check marks indicating message delivery and read status |
| **Online Status** | Live presence indicators showing user availability |

### Group Study Session

| Feature | Description |
|---------|-------------|
| **Create Session** | Initialize collaborative study rooms with customizable settings |
| **Join Session** | Participate in existing study sessions via invite or public discovery |
| **Group Chat** | Real-time messaging within study sessions with participant management |

---

## 2. Architecture

### Modular Architecture

The system is designed with a **modular monolith** approach, enabling clear separation of concerns while maintaining deployment simplicity. Each module is self-contained and communicates through well-defined interfaces.

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│                    (Controllers / REST API)                  │
├─────────────────────────────────────────────────────────────┤
│                      Application Layer                       │
│                  (Services / Use Cases / DTOs)               │
├─────────────────────────────────────────────────────────────┤
│                        Domain Layer                          │
│               (Entities / Domain Events / Rules)             │
├─────────────────────────────────────────────────────────────┤
│                     Infrastructure Layer                     │
│          (Repositories / External Services / Config)         │
└─────────────────────────────────────────────────────────────┘
```

### Layered Structure

#### Controller Layer
Handles HTTP requests and WebSocket connections, delegating business logic to services.

```java
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        MessageResponse response = messageService.sendMessage(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/conversation/{userId}")
    public ResponseEntity<Page<MessageResponse>> getConversation(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        
        Page<MessageResponse> messages = messageService.getConversation(
                principal.getId(), userId, pageable);
        return ResponseEntity.ok(messages);
    }
}
```

#### Service Layer (Interface + Implementation)

**Service Interface:**
```java
public interface MessageService {

    MessageResponse sendMessage(SendMessageRequest request, Long senderId);

    Page<MessageResponse> getConversation(Long userId1, Long userId2, Pageable pageable);

    void markAsRead(Long messageId, Long userId);

    void deleteMessage(Long messageId, Long userId);
}
```

**Service Implementation:**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request, Long senderId) {
        log.info("Processing message from user {} to user {}", senderId, request.getRecipientId());

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException(senderId));

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new UserNotFoundException(request.getRecipientId()));

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(request.getContent())
                .messageType(request.getType())
                .status(MessageStatus.SENT)
                .build();

        Message savedMessage = messageRepository.save(message);

        // Publish domain event
        eventPublisher.publishEvent(new MessageSentEvent(this, savedMessage));

        log.info("Message {} sent successfully", savedMessage.getId());
        return messageMapper.toResponse(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversation(Long userId1, Long userId2, Pageable pageable) {
        return messageRepository
                .findConversation(userId1, userId2, pageable)
                .map(messageMapper::toResponse);
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        if (!message.getRecipient().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Cannot mark others' messages as read");
        }

        message.markAsRead();
        messageRepository.save(message);

        eventPublisher.publishEvent(new MessageReadEvent(this, message));
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        if (!message.getSender().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Cannot delete others' messages");
        }

        messageRepository.delete(message);
        log.info("Message {} deleted by user {}", messageId, userId);
    }
}
```

#### Repository Layer
```java
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
        SELECT m FROM Message m 
        WHERE (m.sender.id = :userId1 AND m.recipient.id = :userId2)
           OR (m.sender.id = :userId2 AND m.recipient.id = :userId1)
        ORDER BY m.createdAt DESC
        """)
    Page<Message> findConversation(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.recipient.id = :userId AND m.status = 'SENT'")
    List<Message> findUnreadMessages(@Param("userId") Long userId);
}
```

#### Entity Layer
```java
@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    @Column
    private LocalDateTime readAt;

    public void markAsRead() {
        this.status = MessageStatus.READ;
        this.readAt = LocalDateTime.now();
    }
}
```

### Dependency Inversion Principle

The architecture strictly follows DIP to ensure high-level modules don't depend on low-level modules:

```
┌─────────────────┐         ┌─────────────────┐
│   Controller    │────────▶│ Service Interface│
└─────────────────┘         └────────┬────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │  Service Impl   │
                            └────────┬────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │   Repository    │
                            │   (Interface)   │
                            └─────────────────┘
```

### Avoiding Tight Coupling

- **Interface-based design**: All services expose interfaces, enabling easy mocking and testing
- **Event-driven communication**: Modules communicate via domain events instead of direct calls
- **Dependency Injection**: Spring's IoC container manages all dependencies
- **DTOs for boundaries**: Data Transfer Objects isolate layers from entity changes

---

## 3. Real-Time Communication

### Spring WebSocket

WebSocket enables full-duplex communication channels over a single TCP connection, perfect for real-time messaging.

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
```

### STOMP Protocol

STOMP (Simple Text Oriented Messaging Protocol) provides a standardized way to communicate over WebSockets.

```java
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage,
                           @Header("simpSessionId") String sessionId,
                           Principal principal) {
        
        log.debug("Received message from session {}", sessionId);

        // Process and save message
        MessageResponse response = messageService.sendMessage(
                chatMessage.toRequest(), 
                getUserId(principal));

        // Send to recipient's personal queue
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId().toString(),
                "/queue/messages",
                response);
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingIndicator indicator,
                            Principal principal) {
        
        messagingTemplate.convertAndSendToUser(
                indicator.getRecipientId().toString(),
                "/queue/typing",
                new TypingNotification(getUserId(principal), indicator.isTyping()));
    }

    @MessageMapping("/chat.read")
    public void markAsRead(@Payload ReadReceipt receipt,
                          Principal principal) {
        
        messageService.markAsRead(receipt.getMessageId(), getUserId(principal));
    }
}
```

### Event-Driven Communication

WebSocket events trigger domain events for decoupled processing:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = accessor.getUser().getName();
        
        presenceService.setOnline(username);
        broadcastPresenceUpdate(username, true);
        
        log.info("User connected: {}", username);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = accessor.getUser().getName();
        
        presenceService.setOffline(username);
        broadcastPresenceUpdate(username, false);
        
        log.info("User disconnected: {}", username);
    }

    private void broadcastPresenceUpdate(String username, boolean online) {
        messagingTemplate.convertAndSend("/topic/presence",
                new PresenceUpdate(username, online, Instant.now()));
    }
}
```

---

## 4. Event-Driven Design

### Domain Events

Domain events represent significant occurrences within the business domain:

```java
@Getter
public class MessageSentEvent extends ApplicationEvent {

    private final Long messageId;
    private final Long senderId;
    private final Long recipientId;
    private final String content;
    private final MessageType messageType;
    private final Instant occurredAt;

    public MessageSentEvent(Object source, Message message) {
        super(source);
        this.messageId = message.getId();
        this.senderId = message.getSender().getId();
        this.recipientId = message.getRecipient().getId();
        this.content = message.getContent();
        this.messageType = message.getMessageType();
        this.occurredAt = Instant.now();
    }
}
```

```java
@Getter
public class SessionCreatedEvent extends ApplicationEvent {

    private final Long sessionId;
    private final String sessionName;
    private final Long creatorId;
    private final Set<Long> participantIds;
    private final Instant occurredAt;

    public SessionCreatedEvent(Object source, StudySession session) {
        super(source);
        this.sessionId = session.getId();
        this.sessionName = session.getName();
        this.creatorId = session.getCreator().getId();
        this.participantIds = session.getParticipants().stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        this.occurredAt = Instant.now();
    }
}
```

### ApplicationEventPublisher

Service layer publishes events without knowing about listeners:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class StudySessionServiceImpl implements StudySessionService {

    private final StudySessionRepository sessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public StudySessionResponse createSession(CreateSessionRequest request, Long creatorId) {
        log.info("Creating study session '{}' by user {}", request.getName(), creatorId);

        StudySession session = StudySession.builder()
                .name(request.getName())
                .description(request.getDescription())
                .maxParticipants(request.getMaxParticipants())
                .creator(userRepository.getReferenceById(creatorId))
                .status(SessionStatus.ACTIVE)
                .build();

        StudySession savedSession = sessionRepository.save(session);

        // Publish domain event for decoupled handling
        eventPublisher.publishEvent(new SessionCreatedEvent(this, savedSession));

        log.info("Study session {} created successfully", savedSession.getId());
        return sessionMapper.toResponse(savedSession);
    }
}
```

### Event Listeners

Listeners handle events asynchronously and independently:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final MessageMapper messageMapper;

    @Async
    @EventListener
    public void handleMessageSent(MessageSentEvent event) {
        log.debug("Handling MessageSentEvent for message {}", event.getMessageId());

        // Send real-time notification via WebSocket
        MessageNotification notification = MessageNotification.builder()
                .messageId(event.getMessageId())
                .senderId(event.getSenderId())
                .content(truncateContent(event.getContent()))
                .timestamp(event.getOccurredAt())
                .build();

        messagingTemplate.convertAndSendToUser(
                event.getRecipientId().toString(),
                "/queue/messages",
                notification);

        // Send push notification if user is offline
        notificationService.sendPushNotificationIfOffline(
                event.getRecipientId(), notification);

        log.debug("MessageSentEvent handled successfully");
    }

    @Async
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageRead(MessageReadEvent event) {
        log.debug("Handling MessageReadEvent for message {}", event.getMessageId());

        ReadReceiptNotification receipt = new ReadReceiptNotification(
                event.getMessageId(),
                event.getReadAt());

        messagingTemplate.convertAndSendToUser(
                event.getSenderId().toString(),
                "/queue/read-receipts",
                receipt);
    }

    private String truncateContent(String content) {
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
}
```

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleSessionCreated(SessionCreatedEvent event) {
        log.info("Processing SessionCreatedEvent for session {}", event.getSessionId());

        SessionNotification notification = SessionNotification.builder()
                .sessionId(event.getSessionId())
                .sessionName(event.getSessionName())
                .creatorId(event.getCreatorId())
                .type(NotificationType.SESSION_CREATED)
                .timestamp(event.getOccurredAt())
                .build();

        // Notify all participants
        event.getParticipantIds().forEach(participantId -> {
            messagingTemplate.convertAndSendToUser(
                    participantId.toString(),
                    "/queue/sessions",
                    notification);
        });

        log.info("SessionCreatedEvent processed, {} participants notified",
                event.getParticipantIds().size());
    }

    @Async
    @EventListener
    public void handleUserJoinedSession(UserJoinedSessionEvent event) {
        log.info("User {} joined session {}", event.getUserId(), event.getSessionId());

        // Broadcast to session topic
        messagingTemplate.convertAndSend(
                "/topic/session/" + event.getSessionId(),
                new UserJoinedNotification(event.getUserId(), event.getUsername()));
    }
}
```

### Decoupled Notification Handling

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final EmailService emailService;
    private final PushNotificationService pushService;
    private final UserPreferenceService preferenceService;

    @Async
    @EventListener
    public void handleNotificationRequired(NotificationRequiredEvent event) {
        UserPreferences preferences = preferenceService.getPreferences(event.getUserId());

        if (preferences.isEmailNotificationsEnabled()) {
            emailService.sendNotificationEmail(event);
        }

        if (preferences.isPushNotificationsEnabled()) {
            pushService.sendPushNotification(event);
        }

        log.debug("Notification dispatched for user {} via configured channels",
                event.getUserId());
    }
}
```

---

## 5. Exception Handling

### Custom Exceptions

```java
public abstract class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
```

```java
public class MessageNotFoundException extends BaseException {

    public MessageNotFoundException(Long messageId) {
        super(ErrorCode.MESSAGE_NOT_FOUND,
              String.format("Message with ID %d not found", messageId));
    }
}
```

```java
public class UserNotFoundException extends BaseException {

    public UserNotFoundException(Long userId) {
        super(ErrorCode.USER_NOT_FOUND,
              String.format("User with ID %d not found", userId));
    }
}
```

```java
public class UnauthorizedAccessException extends BaseException {

    public UnauthorizedAccessException(String message) {
        super(ErrorCode.UNAUTHORIZED_ACCESS, message);
    }
}
```

```java
public class SessionFullException extends BaseException {

    public SessionFullException(Long sessionId) {
        super(ErrorCode.SESSION_FULL,
              String.format("Study session %d has reached maximum capacity", sessionId));
    }
}
```

### Standard API Error Response

```java
@Getter
@Builder
public class ApiErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String code;
    private final String message;
    private final String path;
    private final List<FieldError> fieldErrors;

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
    }
}
```

### Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiErrorResponse> handleBaseException(
            BaseException ex, HttpServletRequest request) {
        
        log.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(ex.getErrorCode().getHttpStatus().value())
                .error(ex.getErrorCode().getHttpStatus().getReasonPhrase())
                .code(ex.getErrorCode().name())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        log.warn("Validation error on request to {}", request.getRequestURI());

        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ApiErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .toList();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .code("VALIDATION_ERROR")
                .message("Request validation failed")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        log.warn("Access denied for request to {}", request.getRequestURI());

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .code("ACCESS_DENIED")
                .message("You don't have permission to access this resource")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error processing request to {}", request.getRequestURI(), ex);

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

---

## 6. Logging

### SLF4J Usage

All logging is performed using SLF4J with Logback as the implementation:

```java
@Slf4j  // Lombok annotation for SLF4J Logger
@Service
public class MessageServiceImpl implements MessageService {

    // Logger is automatically created as:
    // private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);
}
```

### Business Logging

```java
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Override
    public MessageResponse sendMessage(SendMessageRequest request, Long senderId) {
        // Entry logging with correlation
        log.info("Processing message request - senderId: {}, recipientId: {}, type: {}",
                senderId, request.getRecipientId(), request.getType());

        // Debug for detailed flow
        log.debug("Message content length: {} characters", request.getContent().length());

        // Success logging
        log.info("Message {} sent successfully from user {} to user {}",
                savedMessage.getId(), senderId, request.getRecipientId());

        return response;
    }
}
```

### Error Logging

```java
@Component
@Slf4j
public class WebSocketEventListener {

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        try {
            // Process disconnect
        } catch (Exception ex) {
            // Error logging with stack trace
            log.error("Failed to process disconnect for session {}: {}",
                    sessionId, ex.getMessage(), ex);
        }
    }
}
```

### Avoid Sensitive Data Logging

```java
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResponse authenticate(LoginRequest request) {
        // CORRECT: Log username only, never password
        log.info("Authentication attempt for user: {}", request.getUsername());

        // WRONG: Never do this!
        // log.debug("Login attempt - user: {}, password: {}", request.getUsername(), request.getPassword());

        // CORRECT: Mask sensitive data if needed
        log.debug("Processing authentication for user: {}", maskEmail(request.getUsername()));
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int atIndex = email.indexOf("@");
        return email.substring(0, Math.min(2, atIndex)) + "***" + email.substring(atIndex);
    }
}
```

### Logging Configuration

```yaml
# application.yml
logging:
  level:
    root: INFO
    org.example.learnlink: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/learnlink.log
    max-size: 10MB
    max-history: 30
```

---

## 7. Database

### JPA / Hibernate

```java
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "org.example.learnlink.modules.*.repository")
public class JpaConfig {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(auth -> ((UserPrincipal) auth.getPrincipal()).getId());
    }
}
```

### Entity Relationships

```java
@Entity
@Table(name = "messages")
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private StudySession session;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();
}
```

```java
@Entity
@Table(name = "study_sessions")
public class StudySession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToMany
    @JoinTable(
        name = "session_participants",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @OrderBy("createdAt DESC")
    private List<Message> messages = new ArrayList<>();
}
```

### Pagination for Message History

```java
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE (m.sender.id = :userId1 AND m.recipient.id = :userId2)
           OR (m.sender.id = :userId2 AND m.recipient.id = :userId1)
        ORDER BY m.createdAt DESC
        """)
    Page<Message> findConversation(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            Pageable pageable);

    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.session.id = :sessionId
        ORDER BY m.createdAt DESC
        """)
    Page<Message> findBySessionId(
            @Param("sessionId") Long sessionId,
            Pageable pageable);

    @Query(value = """
        SELECT m.* FROM messages m
        WHERE m.recipient_id = :userId
          AND m.status = 'SENT'
          AND m.created_at > :since
        ORDER BY m.created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Message> findRecentUnread(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since,
            @Param("limit") int limit);
}
```

**Usage with Pagination:**

```java
@GetMapping("/conversation/{userId}")
public ResponseEntity<Page<MessageResponse>> getConversation(
        @PathVariable Long userId,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(
            Sort.Order.desc("createdAt")));

    Page<MessageResponse> messages = messageService.getConversation(
            principal.getId(), userId, pageable);

    return ResponseEntity.ok(messages);
}
```

---

## 8. Project Structure

```
learnlink-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/example/learnlink/
│   │   │       ├── LearnLinkApplication.java
│   │   │       │
│   │   │       ├── common/
│   │   │       │   ├── exception/
│   │   │       │   │   ├── BaseException.java
│   │   │       │   │   ├── ErrorCode.java
│   │   │       │   │   └── GlobalExceptionHandler.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── ApiErrorResponse.java
│   │   │       │   │   └── PageResponse.java
│   │   │       │   └── service/
│   │   │       │       └── BaseService.java
│   │   │       │
│   │   │       ├── config/
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── WebSocketConfig.java
│   │   │       │   ├── RedisConfig.java
│   │   │       │   ├── AsyncConfig.java
│   │   │       │   └── JpaConfig.java
│   │   │       │
│   │   │       ├── filter/
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   └── RequestLoggingFilter.java
│   │   │       │
│   │   │       └── modules/
│   │   │           │
│   │   │           ├── auth/
│   │   │           │   ├── controller/
│   │   │           │   │   └── AuthController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── LoginRequest.java
│   │   │           │   │   ├── RegisterRequest.java
│   │   │           │   │   └── AuthResponse.java
│   │   │           │   ├── entity/
│   │   │           │   │   └── RefreshToken.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── AuthService.java
│   │   │           │   │   └── AuthServiceImpl.java
│   │   │           │   └── security/
│   │   │           │       ├── JwtTokenProvider.java
│   │   │           │       └── UserPrincipal.java
│   │   │           │
│   │   │           ├── messaging/
│   │   │           │   ├── controller/
│   │   │           │   │   ├── MessageController.java
│   │   │           │   │   └── ChatWebSocketController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── SendMessageRequest.java
│   │   │           │   │   ├── MessageResponse.java
│   │   │           │   │   ├── ChatMessage.java
│   │   │           │   │   └── TypingIndicator.java
│   │   │           │   ├── entity/
│   │   │           │   │   ├── Message.java
│   │   │           │   │   ├── MessageStatus.java
│   │   │           │   │   ├── MessageType.java
│   │   │           │   │   └── Attachment.java
│   │   │           │   ├── event/
│   │   │           │   │   ├── MessageSentEvent.java
│   │   │           │   │   └── MessageReadEvent.java
│   │   │           │   ├── exception/
│   │   │           │   │   └── MessageNotFoundException.java
│   │   │           │   ├── listener/
│   │   │           │   │   └── MessageEventListener.java
│   │   │           │   ├── mapper/
│   │   │           │   │   └── MessageMapper.java
│   │   │           │   ├── repository/
│   │   │           │   │   └── MessageRepository.java
│   │   │           │   └── service/
│   │   │           │       ├── MessageService.java
│   │   │           │       └── MessageServiceImpl.java
│   │   │           │
│   │   │           ├── session/
│   │   │           │   ├── controller/
│   │   │           │   │   └── StudySessionController.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── CreateSessionRequest.java
│   │   │           │   │   ├── JoinSessionRequest.java
│   │   │           │   │   └── StudySessionResponse.java
│   │   │           │   ├── entity/
│   │   │           │   │   ├── StudySession.java
│   │   │           │   │   └── SessionStatus.java
│   │   │           │   ├── event/
│   │   │           │   │   ├── SessionCreatedEvent.java
│   │   │           │   │   └── UserJoinedSessionEvent.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── SessionNotFoundException.java
│   │   │           │   │   └── SessionFullException.java
│   │   │           │   ├── listener/
│   │   │           │   │   └── SessionEventListener.java
│   │   │           │   ├── repository/
│   │   │           │   │   └── StudySessionRepository.java
│   │   │           │   └── service/
│   │   │           │       ├── StudySessionService.java
│   │   │           │       └── StudySessionServiceImpl.java
│   │   │           │
│   │   │           ├── user/
│   │   │           │   ├── controller/
│   │   │           │   │   └── UserController.java
│   │   │           │   ├── dto/
│   │   │           │   │   └── UserResponse.java
│   │   │           │   ├── entity/
│   │   │           │   │   └── User.java
│   │   │           │   ├── repository/
│   │   │           │   │   └── UserRepository.java
│   │   │           │   └── service/
│   │   │           │       ├── UserService.java
│   │   │           │       └── UserServiceImpl.java
│   │   │           │
│   │   │           ├── presence/
│   │   │           │   ├── dto/
│   │   │           │   │   └── PresenceUpdate.java
│   │   │           │   ├── listener/
│   │   │           │   │   └── WebSocketEventListener.java
│   │   │           │   └── service/
│   │   │           │       ├── PresenceService.java
│   │   │           │       └── PresenceServiceImpl.java
│   │   │           │
│   │   │           └── notification/
│   │   │               ├── dto/
│   │   │               │   └── NotificationPayload.java
│   │   │               ├── listener/
│   │   │               │   └── NotificationEventListener.java
│   │   │               └── service/
│   │   │                   ├── NotificationService.java
│   │   │                   └── PushNotificationService.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── logback-spring.xml
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__Create_Users_Table.sql
│   │               ├── V2__Create_Messages_Table.sql
│   │               └── V3__Create_Study_Sessions_Table.sql
│   │
│   └── test/
│       └── java/
│           └── org/example/learnlink/
│               ├── modules/
│               │   ├── messaging/
│               │   │   ├── service/
│               │   │   │   └── MessageServiceTest.java
│               │   │   └── controller/
│               │   │       └── MessageControllerTest.java
│               │   └── session/
│               │       └── service/
│               │           └── StudySessionServiceTest.java
│               └── integration/
│                   └── MessagingIntegrationTest.java
│
├── docs/
│   ├── API.md
│   ├── ARCHITECTURE.md
│   └── DEPLOYMENT.md
│
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 9. Scalability

### Redis for WebSocket Scaling

Redis enables horizontal scaling of WebSocket connections across multiple server instances:

```java
@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Use Redis as external message broker for scaling
        config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(redisHost)
                .setRelayPort(redisPort)
                .setClientLogin(redisUsername)
                .setClientPasscode(redisPassword);

        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }
}
```

### Message Broker

For high-throughput scenarios, integrate with RabbitMQ or Apache Kafka:

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue messageQueue() {
        return QueueBuilder.durable("message.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .build();
    }

    @Bean
    public TopicExchange messageExchange() {
        return new TopicExchange("message.exchange");
    }

    @Bean
    public Binding messageBinding(Queue messageQueue, TopicExchange messageExchange) {
        return BindingBuilder.bind(messageQueue)
                .to(messageExchange)
                .with("message.#");
    }
}
```

### Caching

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration("users", config.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("sessions", config.entryTtl(Duration.ofMinutes(30)))
                .build();
    }
}
```

```java
@Service
public class UserServiceImpl implements UserService {

    @Cacheable(value = "users", key = "#userId")
    public UserResponse getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @CacheEvict(value = "users", key = "#userId")
    public void updateUser(Long userId, UpdateUserRequest request) {
        // Update logic
    }
}
```

### Scaling Architecture

```
                                    ┌─────────────────┐
                                    │  Load Balancer  │
                                    └────────┬────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
           ┌────────▼────────┐     ┌─────────▼────────┐     ┌────────▼────────┐
           │  App Server 1   │     │   App Server 2   │     │  App Server 3   │
           │  (WebSocket)    │     │   (WebSocket)    │     │  (WebSocket)    │
           └────────┬────────┘     └─────────┬────────┘     └────────┬────────┘
                    │                        │                        │
                    └────────────────────────┼────────────────────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
           ┌────────▼────────┐     ┌─────────▼────────┐     ┌────────▼────────┐
           │     Redis       │     │   PostgreSQL     │     │  Message Broker │
           │  (Pub/Sub +     │     │   (Primary +     │     │   (RabbitMQ)    │
           │   Caching)      │     │    Replicas)     │     │                 │
           └─────────────────┘     └──────────────────┘     └─────────────────┘
```

---

## 10. Sequence Flow

### Sending a Message - Sequence Diagram

```
┌──────┐          ┌──────────┐       ┌─────────────┐       ┌────────────┐       ┌───────────┐       ┌──────────┐
│Client│          │WebSocket │       │ Controller  │       │  Service   │       │Repository │       │  Redis   │
│      │          │  Server  │       │             │       │            │       │           │       │  Pub/Sub │
└──┬───┘          └────┬─────┘       └──────┬──────┘       └─────┬──────┘       └─────┬─────┘       └────┬─────┘
   │                   │                    │                    │                    │                  │
   │ STOMP CONNECT     │                    │                    │                    │                  │
   │──────────────────►│                    │                    │                    │                  │
   │                   │                    │                    │                    │                  │
   │ CONNECTED         │                    │                    │                    │                  │
   │◄──────────────────│                    │                    │                    │                  │
   │                   │                    │                    │                    │                  │
   │ SUBSCRIBE         │                    │                    │                    │                  │
   │ /user/queue/msg   │                    │                    │                    │                  │
   │──────────────────►│                    │                    │                    │                  │
   │                   │                    │                    │                    │                  │
   │ SEND /app/chat    │                    │                    │                    │                  │
   │ {recipient, text} │                    │                    │                    │                  │
   │──────────────────►│                    │                    │                    │                  │
   │                   │                    │                    │                    │                  │
   │                   │ @MessageMapping    │                    │                    │                  │
   │                   │───────────────────►│                    │                    │                  │
   │                   │                    │                    │                    │                  │
   │                   │                    │ sendMessage()      │                    │                  │
   │                   │                    │───────────────────►│                    │                  │
   │                   │                    │                    │                    │                  │
   │                   │                    │                    │ save(message)      │                  │
   │                   │                    │                    │───────────────────►│                  │
   │                   │                    │                    │                    │                  │
   │                   │                    │                    │ savedMessage       │                  │
   │                   │                    │                    │◄───────────────────│                  │
   │                   │                    │                    │                    │                  │
   │                   │                    │                    │ publishEvent       │                  │
   │                   │                    │                    │ (MessageSentEvent) │                  │
   │                   │                    │                    │─────────────────────────────────────► │
   │                   │                    │                    │                    │                  │
   │                   │                    │ MessageResponse    │                    │                  │
   │                   │                    │◄───────────────────│                    │                  │
   │                   │                    │                    │                    │                  │
   │                   │ convertAndSend     │                    │                    │                  │
   │                   │ ToUser(/queue/msg) │                    │                    │                  │
   │                   │◄───────────────────│                    │                    │                  │
   │                   │                    │                    │                    │                  │
   │                   │                    │                    │      ┌─────────────────────────────┐  │
   │                   │                    │                    │      │    EVENT LISTENER           │  │
   │                   │                    │                    │      │    (Async Processing)       │  │
   │                   │                    │                    │      │                             │  │
   │                   │                    │                    │      │ • Send push notification    │  │
   │                   │                    │                    │      │ • Update analytics          │  │
   │                   │                    │                    │      │ • Trigger gamification      │  │
   │                   │                    │                    │      └─────────────────────────────┘  │
   │                   │                    │                    │                    │                  │
   │ MESSAGE           │                    │                    │                    │                  │
   │ (to recipient)    │                    │                    │                    │                  │
   │◄──────────────────│                    │                    │                    │                  │
   │                   │                    │                    │                    │                  │
┌──┴───┐          ┌────┴─────┐       ┌──────┴──────┐       ┌─────┴──────┐       ┌─────┴─────┐       ┌────┴─────┐
│Client│          │WebSocket │       │ Controller  │       │  Service   │       │Repository │       │  Redis   │
│      │          │  Server  │       │             │       │            │       │           │       │  Pub/Sub │
└──────┘          └──────────┘       └─────────────┘       └────────────┘       └───────────┘       └──────────┘
```

### Read Receipt Flow

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│  Recipient  │      │   Server    │      │  Database   │      │   Sender    │
└──────┬──────┘      └──────┬──────┘      └──────┬──────┘      └──────┬──────┘
       │                    │                    │                    │
       │  Mark as Read      │                    │                    │
       │  (messageId)       │                    │                    │
       │───────────────────►│                    │                    │
       │                    │                    │                    │
       │                    │  UPDATE status     │                    │
       │                    │  SET 'READ'        │                    │
       │                    │───────────────────►│                    │
       │                    │                    │                    │
       │                    │  MessageReadEvent  │                    │
       │                    │  (publish)         │                    │
       │                    │────────────────────┼───────────────────►│
       │                    │                    │                    │
       │                    │                    │     ✓✓ Read Receipt│
       │                    │                    │                    │
└──────┴──────┘      └──────┴──────┘      └──────┴──────┘      └──────┴──────┘
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Redis 7+
- Docker (optional)

### Quick Start

```bash
# Clone the repository
git clone https://github.com/your-org/learnlink-backend.git
cd learnlink-backend

# Start infrastructure with Docker
docker-compose up -d postgres redis

# Run the application
./mvnw spring-boot:run

# Or build and run
./mvnw clean package
java -jar target/learnlink-*.jar
```

### Configuration

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/learnlink
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}

  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

server:
  port: 8080

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

### API Documentation

Once the application is running, access the API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Acknowledgments

- Spring Boot Team
- STOMP Protocol Contributors
- Open Source Community

---

<p align="center">
  Built with ❤️ using Spring Boot
</p>
