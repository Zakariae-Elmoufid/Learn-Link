package org.example.learnlink.modules.community.mapper;

import org.example.learnlink.modules.community.dto.PageResponse;
import org.example.learnlink.modules.community.dto.PostResponse;
import org.example.learnlink.modules.community.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

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


    default PageResponse<PostResponse> toPageResponse(Page<PostResponse> page) {
        if (page == null) {
            return null;
        }

        PageResponse<PostResponse> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());
        return response;
    }
}

