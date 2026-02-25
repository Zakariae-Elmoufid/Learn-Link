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
