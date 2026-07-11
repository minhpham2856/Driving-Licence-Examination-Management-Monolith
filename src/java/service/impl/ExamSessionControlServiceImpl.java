// Forced recompilation trigger
package service.impl;

import service.ExamSessionControlService;

import dto.ExaminerSlotDTO;
import dao.ExamSessionDAO;
import dao.ExaminerAssignmentDAO;
import dao.impl.ExamSessionDAOImpl;
import dao.impl.ExaminerAssignmentDAOImpl;
import dto.SessionDTO;
import java.sql.Timestamp;
import java.util.List;
import util.examstaff.ExamScheduleRules;
import util.examstaff.ExaminerAssignmentRules;
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
            return "kỳ thi";
        }
        String name = session.getSessionName() != null && !session.getSessionName().isBlank()
                ? session.getSessionName().trim()
                : "kỳ thi";
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
            return StartResult.fail("Không tìm thấy kỳ thi.");
        }
        if (!enums.ExamSessionStatus.canStart(examSession.getStatus())) {
            if (enums.ExamSessionStatus.isInProgress(examSession.getStatus())) {
                return StartResult.fail("Kỳ thi \"" + examSession.getSessionName() + "\" đã được bắt đầu.");
            }
            return StartResult.fail("Kỳ thi \"" + examSession.getSessionName()
                    + "\" không thể bắt đầu (trạng thái: " + examSession.getStatus() + ").");
        }

        List<ExaminerSlotDTO> assignments = assignmentDAO.getBySessionId(sessionId);
        String coverageError = ExaminerAssignmentRules.validateStartCoverage(assignments);
        if (coverageError != null) {
            return StartResult.fail(coverageError);
        }

        Timestamp scheduledStart = examSession.getScheduledStartAt() != null
                ? examSession.getScheduledStartAt()
                : examSession.getCreatedAt();
        if (ExamScheduleRules.isBeforeScheduledStart(scheduledStart)) {
            return StartResult.fail("Chưa đến giờ bắt đầu kỳ thi. Kỳ thi được mở từ "
                    + ExamScheduleRules.formatScheduledStart(scheduledStart) + ".");
        }

        if (!sessionDAO.updateStatus(sessionId, enums.ExamSessionStatus.DANG_DIEN_RA.getDisplayName())) {
            return StartResult.fail("Không cập nhật được trạng thái kỳ thi. Vui lòng thử lại.");
        }

        return StartResult.ok(buildSessionLabel(examSession), examSession.getExamDate(), assignments.size());
    }

    @Override
    public EndResult endSession(int sessionId) {
        SessionDTO examSession = sessionDAO.getById(sessionId);
        if (examSession == null) {
            return EndResult.fail("Không tìm thấy kỳ thi.");
        }
        if (!enums.ExamSessionStatus.isInProgress(examSession.getStatus())) {
            return EndResult.fail("Kỳ thi \"" + examSession.getSessionName()
                    + "\" chưa ở trạng thái đang diễn ra (hiện tại: " + examSession.getStatus() + ").");
        }
        Timestamp endTime = new Timestamp(System.currentTimeMillis());
        if (!sessionDAO.finishSession(sessionId, enums.ExamSessionStatus.HOAN_TAT.getDisplayName(), endTime)) {
            return EndResult.fail("Không cập nhật được trạng thái kết thúc kỳ thi. Vui lòng thử lại.");
        }
        return EndResult.ok(buildSessionLabel(examSession), examSession.getExamDate());
    }

    @Override
    public List<ExaminerSlotDTO> getLoginEligibleAssignments(int examinerUserId) {
        return assignmentDAO.getInProgressAssignmentsForExaminer(examinerUserId);
    }
}






