// Forced recompilation trigger
package examstaff.service.impl;

import examstaff.service.ExamSessionControlService;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.dao.ExamSessionDAO;
import examstaff.dao.ExaminerAssignmentDAO;
import examstaff.dao.impl.ExamSessionDAOImpl;
import examstaff.dao.impl.ExaminerAssignmentDAOImpl;
import examstaff.dto.ExamSummaryDTO;
import java.sql.Timestamp;
import java.util.List;
import examstaff.util.ExamScheduleRules;
import examstaff.util.ExaminerAssignmentRules;
import java.text.SimpleDateFormat;
import java.util.Locale;
import shared.enums.ExamSessionStatus;

public class ExamSessionControlServiceImpl implements ExamSessionControlService {

    public static final String CTX_ACTIVE_EXAM_ID = "examActiveExamId";
    /** Legacy ServletContext attr â€” dual-read khi migrate. */
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

    private static String buildSessionLabel(ExamSummaryDTO session) {
        if (session == null) {
            return "ká»³ thi";
        }
        String name = session.getSessionName() != null && !session.getSessionName().isBlank()
                ? session.getSessionName().trim()
                : "ká»³ thi";
        if (session.getExamDate() == null) {
            return name;
        }
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
                .format(session.getExamDate());
        return name + " - ngÃ y " + date;
    }

    @Override
    public StartResult startExam(int sessionId, int staffUserId) {
        ExamSummaryDTO examSession = sessionDAO.getById(sessionId);
        if (examSession == null) {
            return StartResult.fail("KhÃ´ng tÃ¬m tháº¥y ká»³ thi.");
        }
        if (!shared.enums.ExamSessionStatus.CHUA_DIEN_RA.getValue().equals(examSession.getStatus()) && !shared.enums.ExamSessionStatus.MO.getValue().equals(examSession.getStatus())) {
            if (shared.enums.ExamSessionStatus.DANG_DIEN_RA.getValue().equals(examSession.getStatus())) {
                return StartResult.fail("Ká»³ thi \"" + examSession.getSessionName() + "\" Ä‘Ã£ Ä‘Æ°á»£c báº¯t Ä‘áº§u.");
            }
            return StartResult.fail("Ká»³ thi \"" + examSession.getSessionName()
                    + "\" khÃ´ng thá»ƒ báº¯t Ä‘áº§u (tráº¡ng thÃ¡i: " + examSession.getStatus() + ").");
        }

        List<ExaminerSlotDTO> assignments = assignmentDAO.getByExamId(sessionId);
        String coverageError = ExaminerAssignmentRules.validateStartCoverage(assignments);
        if (coverageError != null) {
            return StartResult.fail(coverageError);
        }

        Timestamp scheduledStart = examSession.getScheduledStartAt() != null
                ? examSession.getScheduledStartAt()
                : examSession.getCreatedAt();
        if (ExamScheduleRules.isBeforeScheduledStart(scheduledStart)) {
            return StartResult.fail("ChÆ°a Ä‘áº¿n giá» báº¯t Ä‘áº§u ká»³ thi. Ká»³ thi Ä‘Æ°á»£c má»Ÿ tá»« "
                    + ExamScheduleRules.formatScheduledStart(scheduledStart) + ".");
        }

        if (!sessionDAO.updateStatus(sessionId, ExamSessionStatus.DANG_DIEN_RA.getValue())) {
            return StartResult.fail("KhÃ´ng cáº­p nháº­t Ä‘Æ°á»£c tráº¡ng thÃ¡i ká»³ thi. Vui lÃ²ng thá»­ láº¡i.");
        }

        return StartResult.ok(buildSessionLabel(examSession), examSession.getExamDate(), assignments.size());
    }

    @Override
    public EndResult endExam(int sessionId) {
        ExamSummaryDTO examSession = sessionDAO.getById(sessionId);
        if (examSession == null) {
            return EndResult.fail("KhÃ´ng tÃ¬m tháº¥y ká»³ thi.");
        }
        if (!shared.enums.ExamSessionStatus.DANG_DIEN_RA.getValue().equals(examSession.getStatus())) {
            return EndResult.fail("Ká»³ thi \"" + examSession.getSessionName()
                    + "\" chÆ°a á»Ÿ tráº¡ng thÃ¡i Ä‘ang diá»…n ra (hiá»‡n táº¡i: " + examSession.getStatus() + ").");
        }
        Timestamp endTime = new Timestamp(System.currentTimeMillis());
        if (!sessionDAO.finishSession(sessionId, ExamSessionStatus.HOAN_TAT.getValue(), endTime)) {
            return EndResult.fail("KhÃ´ng cáº­p nháº­t Ä‘Æ°á»£c tráº¡ng thÃ¡i káº¿t thÃºc ká»³ thi. Vui lÃ²ng thá»­ láº¡i.");
        }
        return EndResult.ok(buildSessionLabel(examSession), examSession.getExamDate());
    }

    @Override
    public List<ExaminerSlotDTO> getLoginEligibleAssignments(int examinerUserId) {
        return assignmentDAO.getInProgressAssignmentsForExaminer(examinerUserId);
    }
}









