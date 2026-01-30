package org.example.learnlink.common.event;

import org.example.learnlink.modules.user.entity.AcademicLevel;

import java.util.List;

public record UserRegisteredEvent (
    long  userId,
    String firstName,
    String lastName,
    String bio,
    List<Long> studentSubjectIds,
    AcademicLevel academicLevel
){
}
