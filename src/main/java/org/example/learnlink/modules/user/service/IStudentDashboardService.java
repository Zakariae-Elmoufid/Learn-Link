package org.example.learnlink.modules.user.service;

import org.example.learnlink.modules.user.dto.StudentDashboardResponse;

public interface IStudentDashboardService {
    StudentDashboardResponse getDashboard(Long userId);
    StudentDashboardResponse getDashboard(Long userId, int activityLimit);
}
