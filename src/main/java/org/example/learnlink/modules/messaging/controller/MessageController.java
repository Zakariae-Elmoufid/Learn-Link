package org.example.learnlink.modules.messaging.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.messaging.dto.ConversationResponse;
import org.example.learnlink.modules.messaging.dto.MessageResponse;
import org.example.learnlink.modules.messaging.dto.PageResponse;
import org.example.learnlink.modules.messaging.dto.SendMessageRequest;
import org.example.learnlink.modules.messaging.mapper.MessageMapper;
import org.example.learnlink.modules.messaging.service.IMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Message management
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final IMessageService messageService;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a new message
     * POST /api/messages
     */
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SendMessageRequest request) {

        log.info("User {} sending message to user {}", userId, request.getRecipientId());
        MessageResponse response = messageService.sendMessage(userId, request);

        messagingTemplate.convertAndSendToUser(
                request.getRecipientId().toString(),
                "/queue/messages",
                response
        );

        messagingTemplate.convertAndSendToUser(
                request.toString(),
                "/queue/messages",
                response
        );

        log.debug("Message {} delivered via WebSocket", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get message by ID
     * GET /api/messages/{messageId}
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> getMessageById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long messageId) {

        MessageResponse response = messageService.getMessageById(messageId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get conversation with another user
     * GET /api/messages/conversation/{otherUserId}
     */
    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<PageResponse<MessageResponse>> getConversation(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MessageResponse> messages = messageService.getConversation(userId, otherUserId, pageable);
        return ResponseEntity.ok(messageMapper.toPageResponse(messages));
    }

    /**
     * Get all conversations for current user
     * GET /api/messages/conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getUserConversations(
            @RequestHeader("X-User-Id") Long userId) {

        List<ConversationResponse> conversations = messageService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Mark a message as read
     * PUT /api/messages/{messageId}/read
     */
    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long messageId) {

        messageService.markAsRead(messageId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all messages in a conversation as read
     * PUT /api/messages/conversation/{senderId}/read
     */
    @PutMapping("/conversation/{senderId}/read")
    public ResponseEntity<Void> markConversationAsRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long senderId) {

        messageService.markConversationAsRead(userId, senderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a message
     * DELETE /api/messages/{messageId}
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long messageId) {

        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete entire conversation
     * DELETE /api/messages/conversation/{otherUserId}
     */
    @DeleteMapping("/conversation/{otherUserId}")
    public ResponseEntity<Void> deleteConversation(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long otherUserId) {

        messageService.deleteConversation(userId, otherUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get unread message count
     * GET /api/messages/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {

        Long count = messageService.countUnreadMessages(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get unread message count in a specific conversation
     * GET /api/messages/conversation/{senderId}/unread/count
     */
    @GetMapping("/conversation/{senderId}/unread/count")
    public ResponseEntity<Long> getUnreadCountInConversation(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long senderId) {

        Long count = messageService.countUnreadInConversation(userId, senderId);
        return ResponseEntity.ok(count);
    }
}
