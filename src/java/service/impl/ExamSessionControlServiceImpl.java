// Forced recompilation trigger
package service.impl;

import service.ExamSessionControlService;

import dto.ExaminerSlotDTO;
import dao.ExamSessionDAO;
import dao.ExaminerAssignmentDAO;
import dao.impl.ExamSessionDAOImpl;
import dao.impl.ExaminerAssignmentDAOImpl;
import dto.SessionDTO;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExamSessionControlServiceImpl implements ExamSessionControlService {

    public static final String CTX_ACTIVE_SESSION_ID = "examActiveSessionId";

    private final ExamSessionDAO sessionDAO;
    private final ExaminerAssignmentDAO assignmentDAO;

    public ExamSessionControlServiceImpl() {
        this(new ExamSessionDAOImpl(), new ExaminerAssignmentDAOImpl());
    }

    public ExamSessionControlServiceImpl(ExamSessionDAO sessionDAO, ExaminerAssignmentDAO assignmentDAO) {
        this.sessionDAO = sessionDAO;
        this.assignmentDAO = assignmentDAO;
    }

    private static String buildSessionLabel(SessionDTO session) {
        if (session == null) {
            return "ca thi";
        }
        String name = session.getSessionName() != null && !session.getSessionName().isBlank()
                ? session.getSessionName().trim()
                : "ca thi";
        if (session.getExamDate() == null) {
            return name;
        }
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
                .format(session.getExamDate());
        return name + " - ngày " + date;
    }

    @Override
    public StartResult startSession(int sessionId, int staffUserId) {
        SessionDTO examSession = sessionDAO.getById(sessionId);
        if (examSession == null) {
            return StartResult.fail("Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!enums.ExamSessionStatus.canStart(examSession.getStatus())) {
            if (enums.ExamSessionStatus.isInProgress(examSession.getStatus())) {
                return StartResult.fail("Ca thi \"" + examSession.getSessionName() + "\" đã được bắt đầu.");
            }
            return StartResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" không thể bắt đầu (trạng thái: " + examSession.getStatus() + ").");
        }

        List<ExaminerSlotDTO> assignments = assignmentDAO.getBySessionId(sessionId);
        long withArea = assignments.stream().filter(s -> s.getAreaId() > 0).count();
        if (withArea == 0) {
            return StartResult.fail("Chưa phân công sát hạch viên vào khu vực thi. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi bắt đầu ca.");
        }

        if (!sessionDAO.updateStatus(sessionId, enums.ExamSessionStatus.DANG_DIEN_RA.getDisplayName())) {
            return StartResult.fail("Không cập nhật được trạng thái ca thi trên cơ sở dữ liệu.");
        }

        return StartResult.ok(buildSessionLabel(examSession), examSession.getExamDate(), (int) withArea);
    }

    @Override
    public EndResult endSession(int sessionId) {
        SessionDTO examSession = sessionDAO.getById(sessionId);
        if (examSession == null) {
            return EndResult.fail("Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!enums.ExamSessionStatus.isInProgress(examSession.getStatus())) {
            return EndResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" chưa ở trạng thái đang diễn ra (hiện tại: " + examSession.getStatus() + ").");
        }
        if (!sessionDAO.updateStatus(sessionId, enums.ExamSessionStatus.HOAN_TAT.getDisplayName())) {
            return EndResult.fail("Không cập nhật được trạng thái kết thúc ca thi.");
        }
        return EndResult.ok(buildSessionLabel(examSession), examSession.getExamDate());
    }

    @Override
    public List<ExaminerSlotDTO> getLoginEligibleAssignments(int examinerUserId) {
        return assignmentDAO.getInProgressAssignmentsForExaminer(examinerUserId);
    }
}






