package org.example.learnlink.modules.messaging.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.config.WebSocketAuthInterceptor.WebSocketPrincipal;
import org.example.learnlink.modules.messaging.dto.*;
import org.example.learnlink.modules.messaging.service.IGroupMessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;


/**
 * DEPRECATED: This controller is no longer active.
 * Group messaging feature has been removed from the project.
 */
//@Controller
@RequiredArgsConstructor
@Slf4j
public class GroupChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IGroupMessageService groupMessageService;

    /**
     * Handle incoming group message via WebSocket.
     * 
     * Client sends to: /app/group.send
     * Message broadcasted to: /topic/group/{groupId}
     */
    @MessageMapping("/group.send")
    public void sendGroupMessage(@Payload GroupChatMessageRequest request, Principal principal) {
        Long senderId = getUserId(principal);
        log.debug("WebSocket group message from user {} to group {}", senderId, request.getGroupId());

        // Convert to service request and save
        GroupMessageRequest serviceRequest = GroupMessageRequest.builder()
                .content(request.getContent())
                .type(request.getType())
                .attachmentUrl(request.getAttachmentUrl())
                .attachmentName(request.getAttachmentName())
                .build();

        // Save message and get response
        GroupMessageResponse response = groupMessageService.sendMessage(
                senderId, 
                request.getGroupId(), 
                serviceRequest
        );

        // Broadcast to all group subscribers
        messagingTemplate.convertAndSend(
                "/topic/group/" + request.getGroupId(),
                response
        );

        log.debug("Group message {} broadcasted to /topic/group/{}", response.getId(), request.getGroupId());
    }

    /**
     * Handle group typing indicator.
     * 
     * Client sends to: /app/group.typing
     * Notification broadcasted to: /topic/group/{groupId}/typing
     */
    @MessageMapping("/group.typing")
    public void handleGroupTyping(@Payload GroupTypingIndicator indicator, Principal principal) {
        Long senderId = getUserId(principal);
        log.trace("Group typing indicator from user {} in group {}: {}", 
                senderId, indicator.getGroupId(), indicator.isTyping());

        GroupTypingNotification notification = GroupTypingNotification.builder()
                .userId(senderId)
                .groupId(indicator.getGroupId())
                .typing(indicator.isTyping())
                .timestamp(LocalDateTime.now())
                .build();

        // Broadcast to all group subscribers (client filters out own user)
        messagingTemplate.convertAndSend(
                "/topic/group/" + indicator.getGroupId() + "/typing",
                notification
        );
    }

    /**
     * Handle read receipt for group messages.
     * 
     * Client sends to: /app/group.read
     * Notification broadcasted to: /topic/group/{groupId}/read
     */
    @MessageMapping("/group.read")
    public void handleReadReceipt(@Payload GroupReadReceipt receipt, Principal principal) {
        Long userId = getUserId(principal);
        log.trace("Group read receipt from user {} for message {} in group {}", 
                userId, receipt.getMessageId(), receipt.getGroupId());

        // Mark message as read
        groupMessageService.markMessageAsRead(userId, receipt.getMessageId());

        // Broadcast read status to group
        GroupReadNotification notification = GroupReadNotification.builder()
                .userId(userId)
                .groupId(receipt.getGroupId())
                .messageId(receipt.getMessageId())
                .readAt(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/group/" + receipt.getGroupId() + "/read",
                notification
        );
    }

    /**
     * Extract user ID from WebSocket principal.
     */
    private Long getUserId(Principal principal) {
        if (principal instanceof WebSocketPrincipal) {
            return ((WebSocketPrincipal) principal).getUserId();
        }
        return Long.parseLong(principal.getName());
    }
}
