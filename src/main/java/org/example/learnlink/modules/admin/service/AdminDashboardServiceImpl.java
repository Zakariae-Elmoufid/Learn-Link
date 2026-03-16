package org.example.learnlink.modules.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.admin.dto.response.DashboardStatsResponse;
import org.example.learnlink.modules.admin.dto.response.SubjectStatDto;
import org.example.learnlink.modules.admin.entity.PlatformStats;
import org.example.learnlink.modules.admin.repository.PlatformStatsRepository;
import org.example.learnlink.modules.user.repository.StudentSubjectRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AdminDashboardService using event-driven architecture.
 * Reads from local PlatformStats entity instead of querying all module repositories.
 * Only queries StudentSubjectRepository for dynamic "top subjects" data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final PlatformStatsRepository platformStatsRepository;
    private final StudentSubjectRepository studentSubjectRepository;

    @Override
    @Cacheable(value = "dashboard-stats", key = "'stats'", unless = "#result == null")
    public DashboardStatsResponse getDashboardStats() {
        log.info("Generating dashboard statistics from local PlatformStats");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Get pre-aggregated stats from local entity
        PlatformStats stats = platformStatsRepository.getStats()
                .orElseGet(() -> {
                    log.warn("PlatformStats not found, returning empty stats");
                    return PlatformStats.builder().build();
                });

        // Calculate task completion rate
        double taskCompletionRate = stats.getTotalTasks() != null && stats.getTotalTasks() > 0 
                ? (double) stats.getCompletedTasks() / stats.getTotalTasks() * 100 
                : 0.0;

        // Top Subjects - still need to query for dynamic ranking
        List<SubjectStatDto> topSubjects = getTopSubjects(5);

        return DashboardStatsResponse.builder()
                // User Statistics
                .totalUsers(safeValue(stats.getTotalUsers()))
                .activeUsersLast7Days(safeValue(stats.getActiveUsersLast7Days()))
                .activeUsersLast30Days(safeValue(stats.getActiveUsersLast30Days()))
                .newUsersThisWeek(safeValue(stats.getNewUsersThisWeek()))
                .newUsersThisMonth(safeValue(stats.getNewUsersThisMonth()))
                // Content Statistics
                .totalPosts(safeValue(stats.getTotalPosts()))
                .totalQuestions(safeValue(stats.getTotalQuestions()))
                .totalAnswers(safeValue(stats.getTotalAnswers()))
                .totalComments(safeValue(stats.getTotalComments()))
                .postsThisWeek(safeValue(stats.getPostsThisWeek()))
                // Task Statistics
                .totalTasks(safeValue(stats.getTotalTasks()))
                .completedTasks(safeValue(stats.getCompletedTasks()))
                .taskCompletionRate(Math.round(taskCompletionRate * 100.0) / 100.0)
                // Engagement Statistics
                .totalConnections(safeValue(stats.getTotalConnections()))
                // Gamification Statistics
                .totalPointsAwarded(safeValue(stats.getTotalPointsAwarded()))
                .badgesEarned(safeValue(stats.getTotalBadgesEarned()))
                // Dynamic Data
                .topSubjects(topSubjects)
                .generatedAt(now)
                .build();
    }

    /**
     * Get top subjects by number of students.
     * This is dynamic data that still needs to be queried.
     */
    private List<SubjectStatDto> getTopSubjects(int limit) {
        try {
            List<Object[]> subjectCounts = studentSubjectRepository.findTopSubjectsWithCount(limit);
            log.info("subjectCounts size: {}", subjectCounts.size());
            long totalStudents = studentSubjectRepository.countTotalStudentSubjectAssociations();
            log.info("totalStudents: {}", totalStudents);
            return subjectCounts.stream()
                    .map(row -> {
                        String name = (String) row[0];
                        Long count = ((Number) row[1]).longValue();
                        Double percentage = totalStudents > 0 
                                ? (double) count / totalStudents * 100 
                                : 0.0;
                        return SubjectStatDto.builder()
                                .subject(name)
                                .count(count)
                                .percentage(Math.round(percentage * 100.0) / 100.0)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to fetch top subjects: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private long safeValue(Long value) {
        return value != null ? value : 0L;
    }
}
