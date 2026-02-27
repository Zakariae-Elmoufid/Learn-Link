package org.example.learnlink.modules.messaging.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.config.WebSocketAuthInterceptor.WebSocketPrincipal;
import org.example.learnlink.modules.messaging.dto.*;
import org.example.learnlink.modules.messaging.service.IMessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;


@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IMessageService messageService;

    /**
     * Handle incoming chat message.
     * 
     * Client sends to: /app/chat.send
     * Message forwarded to: /user/{recipientId}/queue/messages
     * 
     * @param chatMessage The chat message payload
     * @param principal The authenticated user (from WebSocket session)
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest chatMessage, Principal principal) {
        Long senderId = getUserId(principal);
        log.debug("WebSocket message from user {} to user {}", senderId, chatMessage.getRecipientId());

        // Convert to SendMessageRequest and save via service
        SendMessageRequest request = SendMessageRequest.builder()
                .recipientId(chatMessage.getRecipientId())
                .content(chatMessage.getContent())
                .type(chatMessage.getType())
                .attachmentUrl(chatMessage.getAttachmentUrl())
                .attachmentName(chatMessage.getAttachmentName())
                .build();

        // Save message and get response
        log.info("SenderId: {}, RecipientId: {}", senderId, chatMessage.getRecipientId());
        MessageResponse response = messageService.sendMessage(senderId, request);
        // Send message to recipient in real-time
        // The recipient subscribes to /user/queue/messages
        // Spring resolves /user/{recipientId}/queue/messages automatically
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId().toString(),
                "/queue/messages",
                response
        );

        // Also send confirmation back to sender
        messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/queue/messages",
                response
        );

        log.debug("Message {} delivered via WebSocket", response.getId());
    }

    /**
     * Handle typing indicator.
     * 
     * Client sends to: /app/chat.typing
     * Notification forwarded to: /user/{recipientId}/queue/typing
     * 
     * @param indicator The typing indicator payload
     * @param principal The authenticated user
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingIndicator indicator, Principal principal) {
        Long senderId = getUserId(principal);
        log.trace("Typing indicator from user {} to user {}: {}", 
                senderId, indicator.getRecipientId(), indicator.isTyping());

        TypingNotification notification = TypingNotification.builder()
                .senderId(senderId)
                .typing(indicator.isTyping())
                .timestamp(LocalDateTime.now())
                .build();

        // Send typing notification to recipient
        messagingTemplate.convertAndSendToUser(
                indicator.getRecipientId().toString(),
                "/queue/typing",
                notification
        );
    }

    /**
     * Handle read receipt.
     * 
     * Client sends to: /app/chat.read
     * Notification forwarded to: /user/{senderId}/queue/read-receipts
     * 
     * @param receipt The read receipt payload
     * @param principal The authenticated user
     */
    @MessageMapping("/chat.read")
    public void handleReadReceipt(@Payload ReadReceiptRequest receipt, Principal principal) {
        Long readerId = getUserId(principal);
        log.debug("Read receipt for message {} from user {}", receipt.getMessageId(), readerId);

        // Mark message as read in database
        messageService.markAsRead(receipt.getMessageId(), readerId);

        // Get message to find the sender
        MessageResponse message = messageService.getMessageById(receipt.getMessageId(), readerId);

        // Notify the original sender that their message was read
        ReadReceiptNotification notification = ReadReceiptNotification.builder()
                .messageId(receipt.getMessageId())
                .readerId(readerId)
                .readAt(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSendToUser(
                message.getSenderId().toString(),
                "/queue/read-receipts",
                notification
        );
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
