package org.example.learnlink.modules.user.event;

import org.springframework.web.multipart.MultipartFile;

public record UserProfileImageRequestedEvent(
        long userProfileId,
        MultipartFile image
) {
}
