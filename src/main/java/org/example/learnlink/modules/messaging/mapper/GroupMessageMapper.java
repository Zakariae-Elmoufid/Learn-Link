package org.example.learnlink.modules.messaging.mapper;

import org.example.learnlink.modules.messaging.dto.GroupMessageResponse;
import org.example.learnlink.modules.messaging.entity.GroupMessage;
import org.springframework.stereotype.Component;

/**
 * Mapper for GroupMessage entity and DTOs
 */
@Component
public class GroupMessageMapper {

    /**
     * Map GroupMessage entity to GroupMessageResponse DTO
     * 
     * @param message the group message entity
     * @param currentUserId the ID of the current user (for read status)
     * @return the response DTO
     */
    public GroupMessageResponse toResponse(GroupMessage message, Long currentUserId) {
        if (message == null) {
            return null;
        }

        return GroupMessageResponse.builder()
                .id(message.getId())
                .groupId(message.getGroupId())
                .senderId(message.getSenderId())
                // senderName and senderAvatarUrl should be populated by the service layer
                .content(message.getContent())
                .type(message.getMessageType())
                .attachmentUrl(message.getAttachmentKey())
                .attachmentName(message.getAttachmentName())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .readCount(message.getReadCount())
                .readByCurrentUser(message.isReadByUser(currentUserId))
                .build();
    }

    /**
     * Map GroupMessage entity to GroupMessageResponse DTO without current user context
     * 
     * @param message the group message entity
     * @return the response DTO
     */
    public GroupMessageResponse toResponse(GroupMessage message) {
        if (message == null) {
            return null;
        }

        return GroupMessageResponse.builder()
                .id(message.getId())
                .groupId(message.getGroupId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .type(message.getMessageType())
                .attachmentUrl(message.getAttachmentKey())
                .attachmentName(message.getAttachmentName())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .readCount(message.getReadCount())
                .readByCurrentUser(false)
                .build();
    }
}
