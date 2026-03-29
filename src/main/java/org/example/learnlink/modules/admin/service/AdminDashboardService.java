package org.example.learnlink.modules.admin.service;

import org.example.learnlink.modules.admin.dto.response.DashboardStatsResponse;

/**
 * Service for Admin Dashboard operations
 */
public interface AdminDashboardService {
    
    /**
     * Get comprehensive dashboard statistics
     * @return DashboardStatsResponse containing all platform statistics
     */
    DashboardStatsResponse getDashboardStats();
}
