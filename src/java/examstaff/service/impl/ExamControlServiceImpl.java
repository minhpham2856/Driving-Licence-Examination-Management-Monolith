package examstaff.service.impl;

import examstaff.service.ExamControlService;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.dao.ExamDAO;
import examstaff.dao.ExaminerAssignmentDAO;
import examstaff.dao.impl.ExamDAOImpl;
import examstaff.dao.impl.ExaminerAssignmentDAOImpl;
import examstaff.dto.ExamSummaryDTO;
import java.sql.Timestamp;
import java.util.List;
import examstaff.util.ExamScheduleRules;
import examstaff.util.ExaminerAssignmentRules;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExamControlServiceImpl implements ExamControlService {

    public static final String CTX_ACTIVE_EXAM_ID = "examActiveExamId";

    private final ExamDAO examDAO;
    private final ExaminerAssignmentDAO assignmentDAO;

    public ExamControlServiceImpl() {
        this(new ExamDAOImpl(), new ExaminerAssignmentDAOImpl());
    }

    public ExamControlServiceImpl(ExamDAO examDAO, ExaminerAssignmentDAO assignmentDAO) {
        this.examDAO = examDAO;
        this.assignmentDAO = assignmentDAO;
    }

    private static String buildExamLabel(ExamSummaryDTO exam) {
        if (exam == null) {
            return "kỳ thi";
        }
        String name = exam.getExamName() != null && !exam.getExamName().isBlank()
                ? exam.getExamName().trim()
                : "kỳ thi";
        if (exam.getExamDate() == null) {
            return name;
        }
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
                .format(exam.getExamDate());
        return name + " - ngày " + date;
    }

    @Override
    public StartResult startExam(int examId, int staffUserId) {
        ExamSummaryDTO examSummary = examDAO.getById(examId);
        if (examSummary == null) {
            return StartResult.fail("Không tìm thấy kỳ thi.");
        }
        if (!examstaff.enums.ExamStatus.canStart(examSummary.getStatus())) {
            if (examstaff.enums.ExamStatus.isInProgress(examSummary.getStatus())) {
                return StartResult.fail("Kỳ thi \"" + examSummary.getExamName() + "\" đã được bắt đầu.");
            }
            return StartResult.fail("Kỳ thi \"" + examSummary.getExamName()
                    + "\" không thể bắt đầu (trạng thái: " + examSummary.getStatus() + ").");
        }

        List<ExaminerSlotDTO> assignments = assignmentDAO.getByExamId(examId);
        String coverageError = ExaminerAssignmentRules.validateStartCoverage(assignments);
        if (coverageError != null) {
            return StartResult.fail(coverageError);
        }

        Timestamp scheduledStart = examSummary.getScheduledStartAt() != null
                ? examSummary.getScheduledStartAt()
                : examSummary.getCreatedAt();
        if (ExamScheduleRules.isBeforeScheduledStart(scheduledStart)) {
            return StartResult.fail("Chưa đến giờ bắt đầu kỳ thi. Kỳ thi được mở từ "
                    + ExamScheduleRules.formatScheduledStart(scheduledStart) + ".");
        }

        if (!examDAO.updateStatus(examId, examstaff.enums.ExamStatus.DANG_DIEN_RA.getDisplayName())) {
            return StartResult.fail("Không cập nhật được trạng thái kỳ thi. Vui lòng thử lại.");
        }

        return StartResult.ok(buildExamLabel(examSummary), assignments.size());
    }

    @Override
    public EndResult endExam(int examId) {
        ExamSummaryDTO examSummary = examDAO.getById(examId);
        if (examSummary == null) {
            return EndResult.fail("Không tìm thấy kỳ thi.");
        }
        if (!examstaff.enums.ExamStatus.canEnd(examSummary.getStatus())) {
            return EndResult.fail("Kỳ thi \"" + examSummary.getExamName()
                    + "\" chưa thể kết thúc (hiện tại: " + examSummary.getStatus() + ").");
        }
        Timestamp endTime = new Timestamp(System.currentTimeMillis());
        if (!examDAO.finishExam(examId, examstaff.enums.ExamStatus.HOAN_TAT.getDisplayName(), endTime)) {
            return EndResult.fail("Không cập nhật được trạng thái kết thúc kỳ thi. Vui lòng thử lại.");
        }
        return EndResult.ok(buildExamLabel(examSummary));
    }

    @Override
    public PauseResult pauseExam(int examId) {
        ExamSummaryDTO examSummary = examDAO.getById(examId);
        if (examSummary == null) {
            return PauseResult.fail("Không tìm thấy kỳ thi.");
        }
        if (examstaff.enums.ExamStatus.isPaused(examSummary.getStatus())) {
            return PauseResult.ok(buildExamLabel(examSummary));
        }
        if (!examstaff.enums.ExamStatus.isInProgress(examSummary.getStatus())) {
            return PauseResult.fail("Kỳ thi \"" + examSummary.getExamName()
                    + "\" chưa ở trạng thái đang diễn ra (hiện tại: " + examSummary.getStatus() + ").");
        }
        if (!examDAO.updateStatus(examId, examstaff.enums.ExamStatus.TAM_DUNG.getDisplayName())) {
            return PauseResult.fail("Không cập nhật được trạng thái tạm dừng kỳ thi. Vui lòng thử lại.");
        }
        return PauseResult.ok(buildExamLabel(examSummary));
    }

    @Override
    public ResumeResult resumeExam(int examId) {
        ExamSummaryDTO examSummary = examDAO.getById(examId);
        if (examSummary == null) {
            return ResumeResult.fail("Không tìm thấy kỳ thi.");
        }
        if (examstaff.enums.ExamStatus.isInProgress(examSummary.getStatus())) {
            return ResumeResult.ok(buildExamLabel(examSummary));
        }
        if (!examstaff.enums.ExamStatus.isPaused(examSummary.getStatus())) {
            return ResumeResult.fail("Kỳ thi \"" + examSummary.getExamName()
                    + "\" không ở trạng thái tạm dừng (hiện tại: " + examSummary.getStatus() + ").");
        }
        if (!examDAO.updateStatus(examId, examstaff.enums.ExamStatus.DANG_DIEN_RA.getDisplayName())) {
            return ResumeResult.fail("Không cập nhật được trạng thái tiếp tục kỳ thi. Vui lòng thử lại.");
        }
        return ResumeResult.ok(buildExamLabel(examSummary));
    }
}

