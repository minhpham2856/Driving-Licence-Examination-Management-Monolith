package service.impl;

import java.util.*;

import dto.*;
import enums.*;

import dao.ExaminerScheduleDAO;
import service.ExamSessionControlService;

import dto.ExaminerSlotDTO;
import dao.SessionDAO;
import dao.impl.ExaminerScheduleDAOImpl;
import dao.impl.SessionDAOImpl;
import dto.SessionDTO;
import java.util.List;
import service.EnumMappingService;

public class ExamSessionControlServiceImpl implements ExamSessionControlService {
     private final EnumMappingService enumMappingService = new EnumMappingServiceImpl();

    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExaminerScheduleDAO assignmentDAO = new ExaminerScheduleDAOImpl();

    @Override
    public StartResult startSession(int sessionId, int staffUserId) {
        SessionDTO examSession = sessionDAO.getDtoById(sessionId);
        if (examSession == null) {
            return StartResult.fail("Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!enumMappingService.canStartSession(examSession.getStatus())) {
            if (enumMappingService.isSessionInProgress(examSession.getStatus())) {
                return StartResult.fail("Ca thi \"" + examSession.getSessionName() + "\" đã bắt đầu diễn ra.");
            }
            return StartResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" không thể bắt đầu (trạng thái: " + examSession.getStatus() + ").");
        }

        List<ExaminerSlotDTO> assignments = assignmentDAO.getByExamDate(
                examSession.getExamDate(), Map.of(sessionId, examSession.getExamDate()));
        long withArea = assignments.stream().filter(s -> s.getAreaId() > 0).count();
        if (withArea == 0) {
            return StartResult.fail("Chưa phân công sát hạch viên vào khu vực thi. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi bắt đầu ca.");
        }

        if (!sessionDAO.updateStatus(sessionId, ExamSessionStatus.IN_PROGRESS.getStatus())) {
            return StartResult.fail("Không cập nhật được trạng thái ca thi trên cơ sở dữ liệu.");
        }

        return StartResult.ok(examSession.getSessionName(), (int) withArea);
    }

    @Override
    public EndResult endSession(int sessionId) {
        SessionDTO examSession = sessionDAO.getDtoById(sessionId);
        if (examSession == null) {
            return EndResult.fail("Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!enumMappingService.isSessionInProgress(examSession.getStatus())) {
            return EndResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" chưa ở trạng thái đang diễn ra (hiện tại: " + examSession.getStatus() + ").");
        }
        if (!sessionDAO.updateStatus(sessionId, ExamSessionStatus.COMPLETED.getStatus())) {
            return EndResult.fail("Không cập nhật được trạng thái kết thúc ca thi.");
        }
        return EndResult.ok(examSession.getSessionName());
    }

    @Override
    public SessionDTO getSessionById(int id) { return sessionDAO.getDtoById(id); }
    @Override
    public List<SessionDTO> getAllSessions() { return sessionDAO.getAllSessions(); }
    @Override
    public List<SessionDTO> getActiveSessions() { return sessionDAO.getActiveSessions(); }

    @Override
    public List<ExaminerSlotDTO> getLoginEligibleAssignments(int examinerUserId) {
        return assignmentDAO.getInProgressAssignmentsForExaminer(examinerUserId);
    }
}

