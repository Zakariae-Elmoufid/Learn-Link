package org.example.learnlink.modules.media.listeners;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.media.S3StorageService;
import org.example.learnlink.modules.media.dto.UploadResult;
import org.example.learnlink.modules.user.event.UserProfileImageRequestedEvent;
import org.example.learnlink.modules.user.repository.UserProfileRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProfileImageListener {

    private final S3StorageService storageService;
    private final UserProfileRepository userProfileRepository;

    @Async
    @EventListener
    @Transactional
    public void handle(UserProfileImageRequestedEvent event) {
        try {
            // Upload image to S3 using raw bytes (MultipartFile not available in async context)
            UploadResult result = storageService.uploadProfilePictureFromBytes(
                    event.imageData(),
                    event.fileName(),
                    event.contentType(),
                    event.userProfileId()
            );

            // Update the profile with the S3 key (we store key, generate presigned URL on read)
            userProfileRepository.findByUserId(event.userProfileId())
                    .ifPresent(profile -> {
                        profile.setProfilePictureUrl(result.getKey());
                        userProfileRepository.save(profile);
                        log.info("Profile picture updated for user {}: {}", event.userProfileId(), result.getKey());
                    });

        } catch (Exception e) {
            log.error("Failed to upload profile picture for user {}: {}", event.userProfileId(), e.getMessage(), e);
        }
    }
}
