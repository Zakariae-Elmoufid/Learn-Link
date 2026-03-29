package org.example.learnlink.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {
    private DashboardStatistics statistics;
    private List<RecentActivityItem> recentActivities;
    private ContentCreationStats contentCreationStats;
}
