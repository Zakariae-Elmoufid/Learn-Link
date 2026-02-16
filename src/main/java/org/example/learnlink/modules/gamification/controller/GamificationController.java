package org.example.learnlink.modules.gamification.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.gamification.dto.AddPointsRequest;
import org.example.learnlink.modules.gamification.dto.UserPublicProfileResponse;
import org.example.learnlink.modules.gamification.dto.UserScoreResponse;
import org.example.learnlink.modules.gamification.service.GamificationService;
import org.example.learnlink.modules.planner.dto.TaskRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController
{
    private final GamificationService gamificationService;


    @GetMapping("/score")
    public ResponseEntity<UserScoreResponse> getMyScore( @RequestHeader("X-User-Id") Long userId) {
        UserScoreResponse score = gamificationService.getUserScore(userId);
        return ResponseEntity.ok(score);
    }


    @GetMapping("/score/{userId}")
    public ResponseEntity<UserScoreResponse> getUserScore(@PathVariable Long userId) {
        UserScoreResponse score = gamificationService.getUserScore(userId);
        return ResponseEntity.ok(score);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserPublicProfileResponse> getUserPublicProfile(@PathVariable Long userId) {
        UserPublicProfileResponse profile = gamificationService.getUserPublicProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/points")
    public ResponseEntity<UserScoreResponse> addPoints(
            @RequestParam Long userId,
            @Valid @RequestBody AddPointsRequest request) {

        gamificationService.addPoints(userId, request);

        UserScoreResponse response = gamificationService.getUserScore(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

