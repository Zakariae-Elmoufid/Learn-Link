package org.example.learnlink.modules.community.mapper;

import org.example.learnlink.modules.community.dto.PostResponse;
import org.example.learnlink.modules.community.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Post entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface PostMapper {

    /**
     * Map Post entity to PostResponse DTO
     */
    @Mapping(target = "likedByCurrentUser", ignore = true)
    PostResponse postToResponse(Post post);

    /**
     * Map PostResponse DTO to Post entity
     */
    Post responseToPosts(PostResponse response);
}

