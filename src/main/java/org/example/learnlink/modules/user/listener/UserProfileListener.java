package org.example.learnlink.modules.user.listener;


import lombok.RequiredArgsConstructor;
import org.example.learnlink.common.event.UserRegisteredEvent;
import org.example.learnlink.modules.user.entity.StudentSubject;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.example.learnlink.modules.user.repository.StudentSubjectRepository;
import org.example.learnlink.modules.user.repository.UserProfileRepository;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserProfileListener {

    private final UserProfileRepository userProfileRepository;
    private final StudentSubjectRepository studentSubjectRepository;

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        List<StudentSubject> subjects = studentSubjectRepository.findAllById(event.studentSubjectIds());

        UserProfile profile = UserProfile.builder()
                .userId(event.userId())
                .firstName(event.firstName())
                .lastName(event.lastName())
                .bio(event.bio())
                .academicLevel(event.academicLevel())
                .subjects(subjects)
                .build();

        userProfileRepository.save(profile);
    }
}
