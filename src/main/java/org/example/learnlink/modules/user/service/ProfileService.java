package org.example.learnlink.modules.user.service;


import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.media.S3StorageService;
import org.example.learnlink.modules.user.dto.UserProfileCreate;
import org.example.learnlink.modules.user.dto.UserProfileResponse;
import org.example.learnlink.modules.user.dto.UserProfileUpdate;
import org.example.learnlink.modules.user.entity.StudentSubject;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.example.learnlink.modules.user.event.UserProfileImageRequestedEvent;
import org.example.learnlink.modules.user.mapper.UserProfileMapper;
import org.example.learnlink.modules.user.repository.StudentSubjectRepository;
import org.example.learnlink.modules.user.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private StudentSubjectRepository studentSubjectRepository ;
    @Autowired
    private UserProfileMapper mapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private S3StorageService s3StorageService;


    public UserProfileResponse create(long userId , UserProfileCreate request, MultipartFile image ){

        List<StudentSubject> subjects = studentSubjectRepository
                .findAllById(request.studentSubjectIds());


        UserProfile userProfile = UserProfile.builder()
                .userId(userId)
                .bio(request.bio())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .subjects(subjects)
                .academicLevel(request.academicLevel())
                .build();

        UserProfile savedUserProfile = userProfileRepository.save(userProfile);

        // Only publish event if image is provided
        if (image != null && !image.isEmpty()) {
            try {
                // Read bytes BEFORE async processing (MultipartFile not available after request ends)
                byte[] imageData = image.getBytes();
                eventPublisher.publishEvent(
                        new UserProfileImageRequestedEvent(
                                savedUserProfile.getUserId(),
                                imageData,
                                image.getOriginalFilename(),
                                image.getContentType()
                        )
                );
            } catch (IOException e) {
                log.error("Failed to read image file for user {}: {}", userId, e.getMessage());
            }
        }

        return toResponseWithPresignedUrl(savedUserProfile);

    }

    /**
     * Get profile by user ID
     * @param userId the user ID to look up
     * @return UserProfileResponse
     */
    public UserProfileResponse getProfileByUserId(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));

        return toResponseWithPresignedUrl(profile);
    }

    /**
     * Get current user's profile
     * @param userId the authenticated user's ID
     * @return UserProfileResponse
     */
    public UserProfileResponse getMyProfile(Long userId) {
        return getProfileByUserId(userId);
    }

    /**
     * Update user profile
     * @param userId the user ID
     * @param request the update request
     * @param image optional new profile image
     * @return updated UserProfileResponse
     */
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdate request, MultipartFile image) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));

        // Update fields only if provided (not null)
        if (request.firstName() != null) {
            profile.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            profile.setLastName(request.lastName());
        }
        if (request.bio() != null) {
            profile.setBio(request.bio());
        }
        if (request.academicLevel() != null) {
            profile.setAcademicLevel(request.academicLevel());
        }
        if (request.studentSubjectIds() != null && !request.studentSubjectIds().isEmpty()) {
            List<StudentSubject> subjects = studentSubjectRepository.findAllById(request.studentSubjectIds());
            profile.setSubjects(subjects);
        }

        UserProfile updatedProfile = userProfileRepository.save(profile);

        // Handle image update if provided
        if (image != null && !image.isEmpty()) {
            try {
                byte[] imageData = image.getBytes();
                eventPublisher.publishEvent(
                        new UserProfileImageRequestedEvent(
                                updatedProfile.getUserId(),
                                imageData,
                                image.getOriginalFilename(),
                                image.getContentType()
                        )
                );
            } catch (IOException e) {
                log.error("Failed to read image file for user {}: {}", userId, e.getMessage());
            }
        }

        return toResponseWithPresignedUrl(updatedProfile);
    }

    /**
     * Convert UserProfile to UserProfileResponse with presigned URL for profile picture
     */
    private  UserProfileResponse toResponseWithPresignedUrl(UserProfile profile) {
        UserProfileResponse response = mapper.toUserProfileResponse(profile);
        
       String presignedUrl = toPresignedUrl( profile.getProfilePictureUrl());
        
        // Return new response with presigned URL
        return new UserProfileResponse(
                response.firstName(),
                response.lastName(),
                response.bio(),
                presignedUrl,
                response.studentSubjects(),
                response.academicLevel()
        );
    }

    public String toPresignedUrl(String profilePictureUrl) {
        // Check for null or empty string immediately
        if (profilePictureUrl == null || profilePictureUrl.isBlank()) {
            return null;
        }

        try {
            return s3StorageService.generatePresignedUrl(profilePictureUrl);
        } catch (Exception e) {
            log.warn("Failed to generate presigned URL for profile picture: {}. Falling back to raw path.", e.getMessage());
            return profilePictureUrl; // Fallback to avoid breaking the UI
        }
    }
}
