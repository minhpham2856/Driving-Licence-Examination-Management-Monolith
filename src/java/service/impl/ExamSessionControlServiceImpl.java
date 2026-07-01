package service.impl;
import java.util.List;
import dto.ExaminerSlotDTO;
import dto.SessionDTO;
import enums.ExamSessionStatus;
import dao.ExaminerScheduleDAO;
import dao.SessionDAO;
import dao.impl.ExaminerScheduleDAOImpl;
import dao.impl.SessionDAOImpl;
import model.ExaminerSchedule;
import service.ExamSessionControlService;
public class ExamSessionControlServiceImpl implements ExamSessionControlService {
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExaminerScheduleDAO assignmentDAO = new ExaminerScheduleDAOImpl();
    private final SessionViewSupport sessionViewSupport = new SessionViewSupport();
    private final ExaminerSlotViewSupport slotViewSupport = new ExaminerSlotViewSupport();
    @Override
    public StartResult startSession(int sessionId, int staffUserId) {
        SessionDTO examSession = sessionViewSupport.toDto(sessionId);
        if (examSession == null) {
            return StartResult.fail("Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!ExamSessionStatus.canStart(examSession.getStatus())) {
            if (ExamSessionStatus.isInProgress(examSession.getStatus())) {
                return StartResult.fail("Ca thi \"" + examSession.getSessionName() + "\" đã bắt đầu diễn ra.");
            }
            return StartResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" không thể bắt đầu (trạng thái: " + examSession.getStatus() + ").");
        }
        List<ExaminerSchedule> assignments = assignmentDAO.getBySessionId(sessionId);
        long withArea = assignments.stream()
                .filter(schedule -> schedule.getExamAreaId() != null && schedule.getExamAreaId() > 0)
                .count();
        if (withArea == 0) {
            return StartResult.fail("Chưa phân công sát hạch viên vào khu vực thi. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi bắt đầu ca.");
        }
        if (!sessionDAO.updateStatus(sessionId, ExamSessionStatus.DANG_DIEN_RA.getDisplayName())) {
            return StartResult.fail("Không cập nhật được trạng thái ca thi trên cơ sở dữ liệu.");
        }
        return StartResult.ok(examSession.getSessionName(), (int) withArea);
    }
    @Override
    public EndResult endSession(int sessionId) {
        SessionDTO examSession = sessionViewSupport.toDto(sessionId);
        if (examSession == null) {
            return EndResult.fail("Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!ExamSessionStatus.isInProgress(examSession.getStatus())) {
            return EndResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" chưa ở trạng thái đang diễn ra (hiện tại: " + examSession.getStatus() + ").");
        }
        if (!sessionDAO.updateStatus(sessionId, ExamSessionStatus.HOAN_TAT.getDisplayName())) {
            return EndResult.fail("Không cập nhật được trạng thái kết thúc ca thi.");
        }
        return EndResult.ok(examSession.getSessionName());
    }
    @Override
    public SessionDTO getSessionById(int id) {
        return sessionViewSupport.toDto(id);
    }
    @Override
    public List<SessionDTO> getAllSessions() {
        return sessionViewSupport.toDtoList(sessionDAO.findAllOrdered());
    }
    @Override
    public List<SessionDTO> getActiveSessions() {
        return sessionViewSupport.toDtoList(sessionDAO.findActive());
    }
    @Override
    public List<ExaminerSlotDTO> getLoginEligibleAssignments(int examinerUserId) {
        return slotViewSupport.toDtoList(assignmentDAO.findInProgressByExaminerId(examinerUserId));
    }
}
