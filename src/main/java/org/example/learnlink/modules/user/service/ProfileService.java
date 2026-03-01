package org.example.learnlink.modules.user.service;


import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.user.dto.UserProfileCreate;
import org.example.learnlink.modules.user.dto.UserProfileResponse;
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
    private  ApplicationEventPublisher eventPublisher;


    public UserProfileResponse create(long UserId , UserProfileCreate request, MultipartFile image ){

        List<StudentSubject> subjects = studentSubjectRepository
                .findAllById(request.studentSubjectIds());



        UserProfile userProfile = UserProfile.builder()
                .userId(UserId)
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
                log.error("Failed to read image file for user {}: {}", UserId, e.getMessage());
            }
        }

        return mapper.toUserProfileResponse(savedUserProfile);

    }

    /**
     * Get profile by user ID
     * @param userId the user ID to look up
     * @return UserProfileResponse
     */
    public UserProfileResponse getProfileByUserId(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
        return mapper.toUserProfileResponse(profile);
    }

    /**
     * Get current user's profile
     * @param userId the authenticated user's ID
     * @return UserProfileResponse
     */
    public UserProfileResponse getMyProfile(Long userId) {
        return getProfileByUserId(userId);
    }
}
