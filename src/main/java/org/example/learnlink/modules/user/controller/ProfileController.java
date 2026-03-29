package org.example.learnlink.modules.user.controller;

import jakarta.validation.Valid;
import org.example.learnlink.modules.auth.security.CustomUserDetails;
import org.example.learnlink.modules.user.dto.UserProfileCreate;
import org.example.learnlink.modules.user.dto.UserProfileResponse;
import org.example.learnlink.modules.user.dto.UserProfileUpdate;
import org.example.learnlink.modules.user.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> Create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute UserProfileCreate request,
            @RequestParam MultipartFile image) {
        Long userId = userDetails.getId();
        UserProfileResponse response = profileService.create(userId, request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get current authenticated user's profile
     * GET /api/profile/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        UserProfileResponse response = profileService.getMyProfile(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get profile by user ID
     * GET /api/profile/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getProfileByUserId(
            @PathVariable Long userId) {
        UserProfileResponse response = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update current user's profile
     * PUT /api/profile
     */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute UserProfileUpdate request,
            @RequestParam(required = false) MultipartFile image) {
        Long userId = userDetails.getId();
        UserProfileResponse response = profileService.updateProfile(userId, request, image);
        return ResponseEntity.ok(response);
    }
}
