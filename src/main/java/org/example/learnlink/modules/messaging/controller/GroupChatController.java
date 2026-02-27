package org.example.learnlink.modules.messaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.auth.security.CustomUserDetails;
import org.example.learnlink.modules.messaging.dto.GroupMessageRequest;
import org.example.learnlink.modules.messaging.dto.GroupMessageResponse;
import org.example.learnlink.modules.messaging.dto.PageResponse;
import org.example.learnlink.modules.messaging.service.IGroupMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for group chat operations.
 * Provides endpoints for sending messages, fetching history, and managing read status.
 */
@RestController
@RequestMapping("/api/groups/{groupId}/messages")
@RequiredArgsConstructor
@Slf4j
public class GroupChatController {

    private final IGroupMessageService groupMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a message to a group.
     * POST /api/groups/{groupId}/messages
     */
    @PostMapping
    @Operation(summary = "Send message to group", description = "Send a new message to a study group chat")
    public ResponseEntity<GroupMessageResponse> sendMessage(
            @Parameter(description = "ID of the study group") @PathVariable Long groupId,
            @Valid @RequestBody GroupMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        log.info("REST: User {} sending message to group {}", userId, groupId);
        
        GroupMessageResponse response = groupMessageService.sendMessage(userId, groupId, request);
        
        // Broadcast to all group subscribers via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/group/" + groupId,
                response
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get message history for a group.
     * 
     * GET /api/groups/{groupId}/messages?page=0&size=20
     */
    @GetMapping
    @Operation(summary = "Get group messages", description = "Get paginated message history for a group")
    public ResponseEntity<PageResponse<GroupMessageResponse>> getMessages(
            @Parameter(description = "ID of the study group") @PathVariable Long groupId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        log.debug("User {} fetching messages for group {}, page={}, size={}", userId, groupId, page, size);
        
        Page<GroupMessageResponse> messages = groupMessageService.getGroupMessages(
                userId,
                groupId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        
        return ResponseEntity.ok(PageResponse.from(messages));
    }

    /**
     * Mark all messages in a group as read.
     * 
     * PUT /api/groups/{groupId}/messages/read
     */
    @PutMapping("/read")
    @Operation(summary = "Mark all as read", description = "Mark all messages in the group as read")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @Parameter(description = "ID of the study group") @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        log.debug("User {} marking all messages as read in group {}", userId, groupId);
        
        int count = groupMessageService.markAllAsRead(userId, groupId);
        return ResponseEntity.ok(Map.of("messagesRead", count));
    }

    /**
     * Mark a specific message as read.
     * 
     * PUT /api/groups/{groupId}/messages/{messageId}/read
     */
    @PutMapping("/{messageId}/read")
    @Operation(summary = "Mark message as read", description = "Mark a specific message as read")
    public ResponseEntity<Void> markMessageAsRead(
            @Parameter(description = "ID of the study group") @PathVariable Long groupId,
            @Parameter(description = "ID of the message") @PathVariable Long messageId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        log.debug("User {} marking message {} as read in group {}", userId, messageId, groupId);
        
        groupMessageService.markMessageAsRead(userId, messageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get unread message count for current user in a group.
     * 
     * GET /api/groups/{groupId}/messages/unread-count
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Get number of unread messages in the group")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(description = "ID of the study group") @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        Long count = groupMessageService.getUnreadCount(userId, groupId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Get list of users who have read a specific message.
     * 
     * GET /api/groups/{groupId}/messages/{messageId}/readers
     */
    @GetMapping("/{messageId}/readers")
    @Operation(summary = "Get message readers", description = "Get list of user IDs who have read a message")
    public ResponseEntity<List<Long>> getMessageReaders(
            @Parameter(description = "ID of the study group") @PathVariable Long groupId,
            @Parameter(description = "ID of the message") @PathVariable Long messageId
    ) {
        List<Long> readers = groupMessageService.getMessageReaders(messageId);
        return ResponseEntity.ok(readers);
    }
}
