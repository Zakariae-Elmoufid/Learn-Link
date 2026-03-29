package org.example.learnlink.modules.user.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.media.event.ProfileImageUploadedEvent;
import org.example.learnlink.modules.user.repository.UserProfileRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens for profile image upload completion events from the media module.
 * Updates the user profile with the S3 key of the uploaded image.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileImageUploadedListener {

    private final UserProfileRepository userProfileRepository;

    @EventListener
    @Transactional
    public void handleProfileImageUploaded(ProfileImageUploadedEvent event) {
        log.info("Profile image uploaded event received: userId={}, s3Key={}",
                event.userId(), event.s3Key());

        userProfileRepository.findByUserId(event.userId())
                .ifPresentOrElse(
                        profile -> {
                            profile.setProfilePictureUrl(event.s3Key());
                            userProfileRepository.save(profile);
                            log.info("Profile picture URL updated for user {}", event.userId());
                        },
                        () -> log.warn("User profile not found for userId={}", event.userId())
                );
    }
}
