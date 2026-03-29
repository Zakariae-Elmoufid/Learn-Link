package org.example.learnlink.modules.media.event;

/**
 * Event published when a profile image has been successfully uploaded to S3.
 * Consumed by the user module to update the profile picture URL.
 *
 * @param userId the ID of the user whose profile picture was uploaded
 * @param s3Key the S3 key where the image is stored
 */
public record ProfileImageUploadedEvent(
        Long userId,
        String s3Key
) {
}
