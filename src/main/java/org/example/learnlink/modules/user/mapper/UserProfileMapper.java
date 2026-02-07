package org.example.learnlink.modules.user.mapper;

import org.example.learnlink.modules.user.dto.UserProfileResponse;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfileResponse toUserProfileResponse(UserProfile user);

}
