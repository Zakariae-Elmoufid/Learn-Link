package org.example.learnlink.modules.matching.mapper;

import org.example.learnlink.modules.matching.dto.response.ConnectionRequestResponse;
import org.example.learnlink.modules.matching.dto.response.ConnectionResponse;
import org.example.learnlink.modules.matching.entity.Connection;
import org.example.learnlink.modules.matching.entity.ConnectionRequest;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Connection entities to DTOs.
 * Handles mapping of connection requests and established connections.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConnectionMapper {

    /**
     * Map ConnectionRequest entity to response DTO.
     * Note: User profile information must be set separately.
     *
     * @param request the connection request entity
     * @return the response DTO
     */
    @Mapping(target = "status", expression = "java(request.getStatus().name())")
    @Mapping(target = "senderFirstName", ignore = true)
    @Mapping(target = "senderLastName", ignore = true)
    @Mapping(target = "senderProfilePictureUrl", ignore = true)
    @Mapping(target = "receiverFirstName", ignore = true)
    @Mapping(target = "receiverLastName", ignore = true)
    @Mapping(target = "receiverProfilePictureUrl", ignore = true)
    ConnectionRequestResponse toRequestResponse(ConnectionRequest request);

    /**
     * Map ConnectionRequest with sender profile information
     *
     * @param request       the connection request
     * @param senderProfile the sender's profile
     * @return the response DTO with sender info populated
     */
    default ConnectionRequestResponse toRequestResponseWithSender(ConnectionRequest request,
                                                                   UserProfile senderProfile) {
        ConnectionRequestResponse response = toRequestResponse(request);
        if (senderProfile != null) {
            response.setSenderFirstName(senderProfile.getFirstName());
            response.setSenderLastName(senderProfile.getLastName());
            response.setSenderProfilePictureUrl(senderProfile.getProfilePictureUrl());
        }
        return response;
    }

    /**
     * Map ConnectionRequest with both sender and receiver profile information
     *
     * @param request         the connection request
     * @param senderProfile   the sender's profile
     * @param receiverProfile the receiver's profile
     * @return the response DTO with all info populated
     */
    default ConnectionRequestResponse toRequestResponseWithProfiles(ConnectionRequest request,
                                                                     UserProfile senderProfile,
                                                                     UserProfile receiverProfile) {
        ConnectionRequestResponse response = toRequestResponseWithSender(request, senderProfile);
        if (receiverProfile != null) {
            response.setReceiverFirstName(receiverProfile.getFirstName());
            response.setReceiverLastName(receiverProfile.getLastName());
            response.setReceiverProfilePictureUrl(receiverProfile.getProfilePictureUrl());
        }
        return response;
    }

    /**
     * Map Connection entity to response DTO.
     * Note: Connected user information must be set separately.
     *
     * @param connection the connection entity
     * @return the response DTO
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", expression = "java(connection.getStatus().name())")
    @Mapping(target = "connectedUserId", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "profilePictureUrl", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "academicLevel", ignore = true)
    ConnectionResponse toConnectionResponse(Connection connection);

    /**
     * Map Connection with the connected user's profile
     *
     * @param connection      the connection entity
     * @param connectedUserId the ID of the connected user (the other person)
     * @param userProfile     the connected user's profile
     * @return the response DTO with user info populated
     */
    default ConnectionResponse toConnectionResponseWithProfile(Connection connection,
                                                                Long connectedUserId,
                                                                UserProfile userProfile) {
        ConnectionResponse response = toConnectionResponse(connection);
        response.setConnectedUserId(connectedUserId);
        if (userProfile != null) {
            response.setFirstName(userProfile.getFirstName());
            response.setLastName(userProfile.getLastName());
            response.setProfilePictureUrl(userProfile.getProfilePictureUrl());
            response.setBio(userProfile.getBio());
            response.setAcademicLevel(userProfile.getAcademicLevel() != null
                    ? userProfile.getAcademicLevel().name()
                    : null);
        }
        return response;
    }

    /**
     * Map list of ConnectionRequest entities to response DTOs
     *
     * @param requests list of connection requests
     * @return list of response DTOs
     */
    List<ConnectionRequestResponse> toRequestResponseList(List<ConnectionRequest> requests);
}
