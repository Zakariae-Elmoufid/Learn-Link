package org.example.learnlink.modules.media.listeners;


import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.media.S3StorageService;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.example.learnlink.modules.user.event.UserProfileImageRequestedEvent;
import org.example.learnlink.modules.user.repository.UserProfileRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserProfileImageListener {


    private final S3StorageService storageService;
    private final UserProfileRepository userProfileRepository;

    @Async
    @EventListener
    public void handle(UserProfileImageRequestedEvent event) throws IOException {

        storageService.upload(
                "profiles/" + event.userProfileId(),
                event.image()
        );

    }
}
