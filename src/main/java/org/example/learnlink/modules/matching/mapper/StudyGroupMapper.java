package org.example.learnlink.modules.matching.mapper;

import org.example.learnlink.modules.matching.dto.request.CreateStudyGroupDto;
import org.example.learnlink.modules.matching.dto.response.GroupMemberResponse;
import org.example.learnlink.modules.matching.dto.response.StudyGroupResponse;
import org.example.learnlink.modules.matching.entity.GroupMembership;
import org.example.learnlink.modules.matching.entity.StudyGroup;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for StudyGroup entities to DTOs.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StudyGroupMapper {

    /**
     * Map CreateStudyGroupDto to StudyGroup entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "memberships", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    StudyGroup toEntity(CreateStudyGroupDto dto);

    /**
     * Map StudyGroup entity to response DTO (basic)
     */
    @Mapping(target = "currentMemberCount", expression = "java(group.getActiveMemberCount())")
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "ownerName", ignore = true)
    @Mapping(target = "isMember", ignore = true)
    @Mapping(target = "isAdmin", ignore = true)
    @Mapping(target = "isOwner", ignore = true)
    @Mapping(target = "hasPendingRequest", ignore = true)
    @Mapping(target = "members", ignore = true)
    StudyGroupResponse toResponse(StudyGroup group);

    /**
     * Map list of StudyGroup entities to response DTOs
     */
    List<StudyGroupResponse> toResponseList(List<StudyGroup> groups);

    /**
     * Map GroupMembership to GroupMemberResponse (basic)
     */
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "profilePictureUrl", ignore = true)
    GroupMemberResponse toMemberResponse(GroupMembership membership);

    /**
     * Map GroupMembership with user profile information
     */
    default GroupMemberResponse toMemberResponseWithProfile(GroupMembership membership, UserProfile profile) {
        GroupMemberResponse response = toMemberResponse(membership);
        if (profile != null) {
            response.setFirstName(profile.getFirstName());
            response.setLastName(profile.getLastName());
            response.setProfilePictureUrl(profile.getProfilePictureUrl());
        }
        return response;
    }

    /**
     * Enrich StudyGroupResponse with user context
     */
    default StudyGroupResponse enrichWithContext(StudyGroupResponse response, 
                                                  Long currentUserId,
                                                  boolean isMember, 
                                                  boolean isAdmin, 
                                                  boolean hasPendingRequest) {
        response.setIsOwner(response.getOwnerId().equals(currentUserId));
        response.setIsMember(isMember);
        response.setIsAdmin(isAdmin);
        response.setHasPendingRequest(hasPendingRequest);
        return response;
    }

    /**
     * Enrich StudyGroupResponse with owner profile
     */
    default StudyGroupResponse enrichWithOwner(StudyGroupResponse response, UserProfile ownerProfile) {
        if (ownerProfile != null) {
            String ownerName = ownerProfile.getFirstName();
            if (ownerProfile.getLastName() != null) {
                ownerName += " " + ownerProfile.getLastName();
            }
            response.setOwnerName(ownerName);
        }
        return response;
    }
}
