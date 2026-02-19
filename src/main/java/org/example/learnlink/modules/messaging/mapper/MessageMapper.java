package org.example.learnlink.modules.messaging.mapper;

import org.example.learnlink.modules.messaging.dto.MessageResponse;
import org.example.learnlink.modules.messaging.dto.PageResponse;
import org.example.learnlink.modules.messaging.entity.Message;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

/**
 * MapStruct mapper for Message entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface MessageMapper {

    /**
     * Map Message entity to MessageResponse DTO
     */
    MessageResponse toResponse(Message message);

    /**
     * Convert Page of MessageResponse to PageResponse
     */
    default PageResponse<MessageResponse> toPageResponse(Page<MessageResponse> page) {
        if (page == null) {
            return null;
        }

        return PageResponse.<MessageResponse>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
