package org.example.learnlink.modules.messaging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.messaging.dto.ConversationResponse;
import org.example.learnlink.modules.messaging.dto.MessageResponse;
import org.example.learnlink.modules.messaging.dto.SendMessageRequest;
import org.example.learnlink.modules.messaging.entity.Message;
import org.example.learnlink.modules.messaging.entity.MessageStatus;
import org.example.learnlink.modules.messaging.event.MessageReadEvent;
import org.example.learnlink.modules.messaging.event.MessageSentEvent;
import org.example.learnlink.modules.messaging.exception.MessageNotFoundException;
import org.example.learnlink.modules.messaging.exception.UnauthorizedMessageAccessException;
import org.example.learnlink.modules.messaging.mapper.MessageMapper;
import org.example.learnlink.modules.messaging.repository.MessageRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of IMessageService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements IMessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        log.info("Sending message from user {} to user {}", senderId, request.getRecipientId());

        Message message = Message.builder()
                .senderId(senderId)
                .recipientId(request.getRecipientId())
                .content(request.getContent())
                .messageType(request.getType())
                .status(MessageStatus.SENT)
                .attachmentUrl(request.getAttachmentUrl())
                .attachmentName(request.getAttachmentName())
                .build();

        Message savedMessage = messageRepository.save(message);

        // Publish event for real-time notification and other listeners
        eventPublisher.publishEvent(new MessageSentEvent(
                this,
                savedMessage.getId(),
                savedMessage.getSenderId(),
                savedMessage.getRecipientId(),
                savedMessage.getMessageType()
        ));

        log.info("Message {} sent successfully", savedMessage.getId());
        return messageMapper.toResponse(savedMessage);

    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse getMessageById(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        // Verify user is part of the conversation
        if (!message.getSenderId().equals(userId) && !message.getRecipientId().equals(userId)) {
            throw new UnauthorizedMessageAccessException(messageId, userId);
        }

        return messageMapper.toResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversation(Long userId, Long otherUserId, Pageable pageable) {
        log.debug("Fetching conversation between user {} and user {}", userId, otherUserId);

        return messageRepository.findConversation(userId, otherUserId, pageable)
                .map(messageMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(Long userId) {
        log.debug("Fetching all conversations for user {}", userId);

        List<Long> partnerIds = messageRepository.findConversationPartners(userId);
        List<ConversationResponse> conversations = new ArrayList<>();

        for (Long partnerId : partnerIds) {
            Message lastMessage = messageRepository.findLastMessageInConversation(userId, partnerId);
            Long unreadCount = messageRepository.countUnreadMessagesFromSender(userId, partnerId);

            conversations.add(ConversationResponse.builder()
                    .participantId(partnerId)
                    .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                    .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : null)
                    .unreadCount(unreadCount)
                    .build());
        }

        // Sort by last message time descending
        conversations.sort((a, b) -> {
            if (a.getLastMessageAt() == null) return 1;
            if (b.getLastMessageAt() == null) return -1;
            return b.getLastMessageAt().compareTo(a.getLastMessageAt());
        });

        return conversations;
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        // Only recipient can mark message as read
        if (!message.getRecipientId().equals(userId)) {
            throw new UnauthorizedMessageAccessException("Only recipient can mark message as read");
        }

        if (message.getStatus() != MessageStatus.READ) {
            message.markAsRead();
            messageRepository.save(message);

            // Publish event for read receipt
            eventPublisher.publishEvent(new MessageReadEvent(
                    this,
                    message.getId(),
                    message.getSenderId(),
                    userId,
                    message.getReadAt()
            ));

            log.debug("Message {} marked as read by user {}", messageId, userId);
        }
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long userId, Long senderId) {
        log.info("Marking all messages from user {} to user {} as read", senderId, userId);

        int updatedCount = messageRepository.markMessagesAsRead(userId, senderId, MessageStatus.READ);
        log.debug("{} messages marked as read", updatedCount);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        // Only sender can delete their message
        if (!message.getSenderId().equals(userId)) {
            throw new UnauthorizedMessageAccessException("Only sender can delete the message");
        }

        messageRepository.delete(message);
        log.info("Message {} deleted by user {}", messageId, userId);
    }

    @Override
    @Transactional
    public void deleteConversation(Long userId, Long otherUserId) {
        log.info("Deleting conversation between user {} and user {}", userId, otherUserId);

        int deletedCount = messageRepository.deleteConversation(userId, otherUserId);
        log.info("{} messages deleted from conversation", deletedCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadMessages(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadInConversation(Long userId, Long senderId) {
        return messageRepository.countUnreadMessagesFromSender(userId, senderId);
    }
}
