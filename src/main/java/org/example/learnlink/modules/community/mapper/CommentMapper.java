package org.example.learnlink.modules.community.mapper;

import org.example.learnlink.modules.community.dto.CommentResponse;
import org.example.learnlink.modules.community.entity.Comment;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for Comment entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {

    /**
     * Map Comment entity to CommentResponse DTO
     */
    CommentResponse commentToResponse(Comment comment);

    /**
     * Map CommentResponse DTO to Comment entity
     */
    Comment responseToComment(CommentResponse response);
}

