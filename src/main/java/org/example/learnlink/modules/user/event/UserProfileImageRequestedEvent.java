package org.example.learnlink.modules.user.event;

public record UserProfileImageRequestedEvent(
        long userProfileId,
        byte[] imageData,
        String fileName,
        String contentType
) {
}
