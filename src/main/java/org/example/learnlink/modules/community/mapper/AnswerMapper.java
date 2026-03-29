package org.example.learnlink.modules.community.mapper;

import org.example.learnlink.modules.community.dto.AnswerResponse;
import org.example.learnlink.modules.community.entity.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Answer entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface AnswerMapper {

    /**
     * Map Answer entity to AnswerResponse DTO
     */
    @Mapping(target = "votedByCurrentUser", ignore = true)
    AnswerResponse answerToResponse(Answer answer);

    /**
     * Map AnswerResponse DTO to Answer entity
     */
    Answer responseToAnswer(AnswerResponse response);
}

