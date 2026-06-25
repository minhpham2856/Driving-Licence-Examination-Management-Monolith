package service.impl;

import dao.AdminStatsDAO;
import dao.impl.AdminStatsDAOImpl;
import dto.admin.RecentActivityDTO;
import service.AdminDashboardService;

import java.util.List;

public class AdminDashboardServiceImpl implements AdminDashboardService {
    
    private final AdminStatsDAO statsDAO = new AdminStatsDAOImpl();

    @Override
    public int getTotalExamCenters() {
        return statsDAO.countExamAreas();
    }

    @Override
    public int getTotalUsers() {
        return statsDAO.countUsers();
    }

    @Override
    public int getTotalExamSessions() {
        return statsDAO.countExams();
    }

    @Override
    public int getTotalComputers() {
        return statsDAO.countDevices();
    }

    @Override
    public List<RecentActivityDTO> getRecentActivities(int limit) {
        return statsDAO.recentActivity(limit);
    }
}
