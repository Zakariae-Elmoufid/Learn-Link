package org.example.learnlink.modules.user.controller;

import jakarta.validation.Valid;
import org.apache.coyote.Request;
import org.example.learnlink.modules.media.S3StorageService;
import org.example.learnlink.modules.planner.dto.TaskResponse;
import org.example.learnlink.modules.user.dto.UserProfileCreate;
import org.example.learnlink.modules.user.dto.UserProfileResponse;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.example.learnlink.modules.user.repository.UserProfileRepository;
import org.example.learnlink.modules.user.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> Create(@RequestHeader("X-User-Id") Long userId, @Valid @ModelAttribute UserProfileCreate request
            ,@RequestParam MultipartFile image){
        UserProfileResponse  response =  profileService.create(userId ,request,image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }





}
