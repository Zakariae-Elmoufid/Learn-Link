package org.example.learnlink.modules.user.mapper;

import org.example.learnlink.modules.user.dto.StudentSubjectResponse;
import org.example.learnlink.modules.user.dto.UserProfileResponse;
import org.example.learnlink.modules.user.entity.StudentSubject;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    
    @Mapping(source = "subjects", target = "studentSubjects")
    UserProfileResponse toUserProfileResponse(UserProfile user);
    
    StudentSubjectResponse toStudentSubjectResponse(StudentSubject subject);
    
    List<StudentSubjectResponse> toStudentSubjectResponseList(List<StudentSubject> subjects);
}
