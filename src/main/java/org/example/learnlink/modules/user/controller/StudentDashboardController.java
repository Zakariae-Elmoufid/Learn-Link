package org.example.learnlink.modules.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.auth.security.CustomUserDetails;
import org.example.learnlink.modules.user.dto.StudentDashboardResponse;
import org.example.learnlink.modules.user.service.IStudentDashboardService;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j

public class StudentDashboardController {

    private final IStudentDashboardService dashboardService;

    /**
     * Get authenticated user's dashboard
     * GET /api/dashboard?activityLimit=10
     */
    @GetMapping
    public ResponseEntity<?> getMyDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "10") int activityLimit) {
            Long userId = userDetails.getId();
            StudentDashboardResponse response = dashboardService.getDashboard(userId, activityLimit);

            return ResponseEntity.ok(response);

    }


}
