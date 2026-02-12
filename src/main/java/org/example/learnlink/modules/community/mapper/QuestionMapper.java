package org.example.learnlink.modules.community.mapper;

import org.example.learnlink.modules.community.dto.QuestionResponse;
import org.example.learnlink.modules.community.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Question entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface QuestionMapper {

    /**
     * Map Question entity to QuestionResponse DTO
     */
    QuestionResponse questionToResponse(Question question);
}


