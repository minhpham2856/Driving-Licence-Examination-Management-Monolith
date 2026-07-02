package service.impl;
import dto.*;
import model.*;
import java.sql.*;
import dao.UserDAO;
import dao.ExamAreaDAO;
import dao.ExamDAO;
import dao.ExamDeviceDAO;
import dao.AuditDAO;
import dao.impl.UserDAOImpl;
import dao.impl.ExamAreaDAOImpl;
import dao.impl.ExamDAOImpl;
import dao.impl.ExamDeviceDAOImpl;
import dao.impl.AuditDAOImpl;
import dto.RecentActivityDTO;
import model.Audit;
import model.User;
import service.AdminDashboardService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private final UserDAO userDAO = new UserDAOImpl();
    private final ExamAreaDAO examAreaDAO = new ExamAreaDAOImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final ExamDeviceDAO examDeviceDAO = new ExamDeviceDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();
    @Override
    public int getTotalExamCenters() {
        return examAreaDAO.countAll();
    }
    @Override
    public int getTotalUsers() {
        return userDAO.countAll();
    }
    @Override
    public int getTotalExamSessions() {
        return examDAO.countAll();
    }
    @Override
    public int getTotalComputers() {
        return examDeviceDAO.countAll();
    }
    @Override
    public List<RecentActivityDTO> getRecentActivities(int limit) {
        List<Audit> logs = auditDAO.getRecentLogs(limit);
        List<Integer> userIds = new ArrayList<>();
        for (Audit log : logs) {
            if (log.getUserId() != null && !userIds.contains(log.getUserId())) {
                userIds.add(log.getUserId());
            }
        }
        List<User> users = userDAO.getAllByIds(userIds);
        List<RecentActivityDTO> list = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Audit log : logs) {
            RecentActivityDTO r = new RecentActivityDTO();
            r.setAction(log.getAction());
            r.setModule(log.getEntityName());
            r.setRecordId(log.getEntityId());
            Timestamp ts = log.getCreatedAt();
            r.setTimestamp((ts == null) ? "" : fmt.format(ts));
            String username = "Hệ thống";
            if (log.getUserId() != null) {
                for (User u : users) {
                    if (u.getUserId() == log.getUserId()) {
                        username = u.getUsername();
                        break;
                    }
                }
            }
            r.setUsername(username);
            list.add(r);
        }
        return list;
    }
}
