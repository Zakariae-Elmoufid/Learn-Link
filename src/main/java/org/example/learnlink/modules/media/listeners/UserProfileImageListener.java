package org.example.learnlink.modules.media.listeners;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.media.S3StorageService;
import org.example.learnlink.modules.media.dto.UploadResult;
import org.example.learnlink.modules.media.event.ProfileImageUploadedEvent;
import org.example.learnlink.modules.user.event.UserProfileImageRequestedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for profile image upload requests and handles S3 upload.
 * After successful upload, fires ProfileImageUploadedEvent for the user module to consume.
 * This decouples the media module from the user module's repository.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserProfileImageListener {

    private final S3StorageService storageService;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @EventListener
    public void handle(UserProfileImageRequestedEvent event) {
        try {
            // Upload image to S3 using raw bytes (MultipartFile not available in async context)
            UploadResult result = storageService.uploadProfilePictureFromBytes(
                    event.imageData(),
                    event.fileName(),
                    event.contentType(),
                    event.userProfileId()
            );

            // Fire event for user module to update the profile
            eventPublisher.publishEvent(new ProfileImageUploadedEvent(
                    event.userProfileId(),
                    result.getKey()
            ));

            log.info("Profile image uploaded for user {}, S3 key: {}", 
                    event.userProfileId(), result.getKey());

        } catch (Exception e) {
            log.error("Failed to upload profile picture for user {}: {}", 
                    event.userProfileId(), e.getMessage(), e);
        }
    }
}
