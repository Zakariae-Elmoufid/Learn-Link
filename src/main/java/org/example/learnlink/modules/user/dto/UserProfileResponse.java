package org.example.learnlink.modules.user.dto;

import org.example.learnlink.modules.user.entity.AcademicLevel;

import javax.security.auth.Subject;
import java.util.List;

public record UserProfileResponse(
        String firstName, String lastName, String bio, String profilePictureUrl, List<StudentSubjectResponse> studentSubjects, AcademicLevel academicLevel
) {
}
