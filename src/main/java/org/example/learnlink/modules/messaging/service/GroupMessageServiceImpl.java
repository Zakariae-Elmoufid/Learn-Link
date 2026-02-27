package org.example.learnlink.modules.messaging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.common.service.GroupValidationService;
import org.example.learnlink.modules.messaging.dto.GroupMessageRequest;
import org.example.learnlink.modules.messaging.dto.GroupMessageResponse;
import org.example.learnlink.modules.messaging.entity.GroupMessage;
import org.example.learnlink.modules.messaging.entity.GroupMessageReadStatus;
import org.example.learnlink.modules.messaging.event.GroupMessageSentEvent;
import org.example.learnlink.modules.messaging.exception.GroupAccessDeniedException;
import org.example.learnlink.modules.messaging.exception.GroupNotFoundException;
import org.example.learnlink.modules.messaging.exception.MessageNotFoundException;
import org.example.learnlink.modules.messaging.mapper.GroupMessageMapper;
import org.example.learnlink.modules.messaging.repository.GroupMessageReadStatusRepository;
import org.example.learnlink.modules.messaging.repository.GroupMessageRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of IGroupMessageService.
 * Handles all group messaging business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMessageServiceImpl implements IGroupMessageService {

    private final GroupMessageRepository groupMessageRepository;
    private final GroupMessageReadStatusRepository readStatusRepository;
    private final GroupValidationService groupValidationService;
    private final GroupMessageMapper groupMessageMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public GroupMessageResponse sendMessage(Long senderId, Long groupId, GroupMessageRequest request) {
        log.info("User {} sending message to group {}", senderId, groupId);

        // Validate group exists
        if (!groupValidationService.groupExists(groupId)) {
            throw new GroupNotFoundException(groupId);
        }

        // Validate user is an active member of the group
        validateMembership(senderId, groupId);

        // Create and save the message
        GroupMessage message = GroupMessage.builder()
                .groupId(groupId)
                .senderId(senderId)
                .content(request.getContent())
                .messageType(request.getType())
                .attachmentKey(request.getAttachmentUrl())
                .attachmentName(request.getAttachmentName())
                .build();

        GroupMessage savedMessage = groupMessageRepository.save(message);

        // Get group member IDs for event
        List<Long> memberIds = groupValidationService.getActiveMemberIds(groupId);

        // Publish event for WebSocket broadcast and gamification
        eventPublisher.publishEvent(new GroupMessageSentEvent(
                this,
                savedMessage.getId(),
                groupId,
                senderId,
                memberIds
        ));

        log.info("Group message {} sent successfully", savedMessage.getId());
        return groupMessageMapper.toResponse(savedMessage, senderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupMessageResponse> getGroupMessages(Long userId, Long groupId, Pageable pageable) {
        log.debug("User {} fetching messages for group {}", userId, groupId);

        // Validate group exists
        if (!groupValidationService.groupExists(groupId)) {
            throw new GroupNotFoundException(groupId);
        }

        // Validate membership
        validateMembership(userId, groupId);

        return groupMessageRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable)
                .map(msg -> groupMessageMapper.toResponse(msg, userId));
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId, Long groupId) {
        log.debug("Marking all messages as read for user {} in group {}", userId, groupId);

        // Validate group exists
        if (!groupValidationService.groupExists(groupId)) {
            throw new GroupNotFoundException(groupId);
        }

        // Validate membership
        validateMembership(userId, groupId);

        return readStatusRepository.markAllAsReadForUser(groupId, userId);
    }

    @Override
    @Transactional
    public void markMessageAsRead(Long userId, Long messageId) {
        GroupMessage message = groupMessageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        // Validate membership
        validateMembership(userId, message.getGroupId());

        // Don't mark own messages as read
        if (message.getSenderId().equals(userId)) {
            return;
        }

        // Check if already read
        if (readStatusRepository.existsByGroupMessageIdAndUserId(messageId, userId)) {
            return;
        }

        GroupMessageReadStatus readStatus = GroupMessageReadStatus.builder()
                .groupMessage(message)
                .userId(userId)
                .readAt(LocalDateTime.now())
                .build();

        readStatusRepository.save(readStatus);
        log.debug("Message {} marked as read by user {}", messageId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId, Long groupId) {
        // Validate group exists
        if (!groupValidationService.groupExists(groupId)) {
            throw new GroupNotFoundException(groupId);
        }

        // Validate membership
        validateMembership(userId, groupId);

        return groupMessageRepository.countUnreadMessages(groupId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getMessageReaders(Long messageId) {
        // Verify message exists
        if (!groupMessageRepository.existsById(messageId)) {
            throw new MessageNotFoundException(messageId);
        }

        return readStatusRepository.findByGroupMessageId(messageId)
                .stream()
                .map(GroupMessageReadStatus::getUserId)
                .collect(Collectors.toList());
    }

    /**
     * Validate that a user is an active member of a group.
     */
    private void validateMembership(Long userId, Long groupId) {
        if (!groupValidationService.isActiveMember(groupId, userId)) {
            throw new GroupAccessDeniedException(groupId, userId);
        }
    }
}
