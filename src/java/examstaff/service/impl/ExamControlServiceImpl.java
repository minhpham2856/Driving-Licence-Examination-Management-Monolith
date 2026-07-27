package examstaff.service.impl;

import examstaff.service.ExamControlService;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.ServiceResult;
import examstaff.dao.ExamDAO;
import examstaff.dao.ExaminerAssignmentDAO;
import examstaff.dao.impl.ExamDAOImpl;
import examstaff.dao.impl.ExaminerAssignmentDAOImpl;
import examstaff.dto.ExamSummaryDTO;
import java.sql.Timestamp;
import java.util.List;
import examstaff.service.impl.support.shared.ExamScheduleRules;
import examstaff.service.impl.support.assign.ExaminerAssignmentRules;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.security.SecureRandom;
import shared.enums.ErrorType;

/**
 * Implementation ExamControlService: điều khiển vòng đời kỳ thi qua ExamDAO.
 *
 * Luồng start / pause / resume / end:
 * - <b>startExam</b> — validate lịch (ExamScheduleRules), đếm SHV phân công
 *       (ExaminerAssignmentRules), cập nhật trạng thái kỳ qua ExamDAO
 * - <b>pauseExam / resumeExam</b> — toggle cờ tạm dừng; giữ nguyên hàng đợi gọi số
 * - <b>endExam</b> — đóng kỳ; SHV không thể đăng nhập kỳ này nữa
 * Hằng CTX_ACTIVE_EXAM_ID dùng để đồng bộ session servlet với kỳ đang active.
 */
public class ExamControlServiceImpl implements ExamControlService {

    public static final String CTX_ACTIVE_EXAM_ID = "examActiveExamId";

    private final ExamDAO examDAO;
    private final ExaminerAssignmentDAO assignmentDAO;

    /** Wiring mặc định khi không inject từ composition root. */
    public ExamControlServiceImpl() {
        this(new ExamDAOImpl(), new ExaminerAssignmentDAOImpl());
    }

    /**
     * Inject dependencies cho unit test / composition root.
     * @param examDAO        DAO kỳ thi
     * @param assignmentDAO  DAO phân công SHV
     */
    public ExamControlServiceImpl(ExamDAO examDAO, ExaminerAssignmentDAO assignmentDAO) {
        this.examDAO = examDAO;
        this.assignmentDAO = assignmentDAO;
    }

    /**
     * Ghép nhãn hiển thị kỳ thi (tên + ngày).
     * @param exam tóm tắt kỳ thi
     * @return nhãn hiển thị
     */
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

    /**
     * Tạo ServiceResult thất bại validation với message.
     * @param message thông báo lỗi
     * @param <T>     kiểu data
     * @return kết quả fail
     */
    private static <T> ServiceResult<T> fail(String message) {
        return ServiceResult.fail(ErrorType.VALIDATION_FAILED, message);
    }

    /**
     * Bắt đầu kỳ thi và cho phép sát hạch viên đăng nhập theo phân công.
     * @param examId      mã kỳ thi
     * @param staffUserId mã nhân viên thực hiện
     * @return kết quả thành công/thất bại kèm thông báo
     */
    @Override
    public ServiceResult<StartExamData> startExam(int examId, int staffUserId) {
        // Load
        ExamSummaryDTO examSummary = examDAO.getById(examId);
        // Validate
        if (examSummary == null) {
            return fail("Không tìm thấy kỳ thi.");
        }
        if (!examstaff.enums.ExamStatus.canStart(examSummary.getStatus())) {
            if (examstaff.enums.ExamStatus.isInProgress(examSummary.getStatus())) {
                return fail("Kỳ thi \"" + examSummary.getExamName() + "\" đã được bắt đầu.");
            }
            return fail("Kỳ thi \"" + examSummary.getExamName()
                    + "\" không thể bắt đầu (trạng thái: " + examSummary.getStatus() + ").");
        }

        List<ExaminerSlotDTO> assignments = assignmentDAO.getByExamId(examId);
        String coverageError = ExaminerAssignmentRules.validateStartCoverage(assignments);
        if (coverageError != null) {
            return fail(coverageError);
        }

        Timestamp scheduledStart = examSummary.getScheduledStartAt() != null
                ? examSummary.getScheduledStartAt()
                : examSummary.getCreatedAt();
        if (ExamScheduleRules.isBeforeScheduledStart(scheduledStart)) {
            return fail("Chưa đến giờ bắt đầu kỳ thi. Kỳ thi được mở từ "
                    + ExamScheduleRules.formatScheduledStart(scheduledStart) + ".");
        }

        // Mutate
        if (!examDAO.updateStatus(examId, examstaff.enums.ExamStatus.DANG_DIEN_RA.getDisplayName())) {
            return fail("Không cập nhật được trạng thái kỳ thi. Vui lòng thử lại.");
        }

        // Result
        String label = buildExamLabel(examSummary);
        int examinerCount = assignments.size();
        StartExamData data = new StartExamData(label, examinerCount);
        return ServiceResult.ok(data,
                "Đã bắt đầu kỳ thi \"" + label + "\". "
                        + examinerCount + " sát hạch viên có thể đăng nhập.");
    }

    /**
     * Kết thúc kỳ thi; sát hạch viên không còn đăng nhập được kỳ này.
     * @param examId mã kỳ thi
     * @return kết quả thành công/thất bại kèm thông báo
     */
    @Override
    public ServiceResult<String> endExam(int examId) {
        // Load
        ExamSummaryDTO examSummary = examDAO.getById(examId);
        // Validate
        if (examSummary == null) {
            return fail("Không tìm thấy kỳ thi.");
        }
        if (!examstaff.enums.ExamStatus.canEnd(examSummary.getStatus())) {
            return fail("Kỳ thi \"" + examSummary.getExamName()
                    + "\" chưa thể kết thúc (hiện tại: " + examSummary.getStatus() + ").");
        }
        // Mutate
        Timestamp endTime = new Timestamp(System.currentTimeMillis());
        if (!examDAO.finishExam(examId, examstaff.enums.ExamStatus.HOAN_TAT.getDisplayName(), endTime)) {
            return fail("Không cập nhật được trạng thái kết thúc kỳ thi. Vui lòng thử lại.");
        }
        // Result
        String label = buildExamLabel(examSummary);
        return ServiceResult.ok(label,
                "Đã kết thúc kỳ thi \"" + label
                        + "\". Sát hạch viên không thể đăng nhập kỳ thi này nữa.");
    }

    /**
     * Tạm dừng kỳ thi; giữ hàng đợi gọi số, khóa đăng nhập sát hạch viên.
     * @param examId mã kỳ thi
     * @return kết quả thành công/thất bại kèm thông báo
     */
    @Override
    public ServiceResult<String> pauseExam(int examId) {
        // Load
        ExamSummaryDTO examSummary = examDAO.getById(examId);
        // Validate
        if (examSummary == null) {
            return fail("Không tìm thấy kỳ thi.");
        }
        String label = buildExamLabel(examSummary);
        String okMessage = "Đã tạm dừng kỳ thi \"" + label
                + "\". Hàng đợi gọi số được giữ nguyên; sát hạch viên không thể đăng nhập khi đang tạm dừng.";
        if (examstaff.enums.ExamStatus.isPaused(examSummary.getStatus())) {
            return ServiceResult.ok(label, okMessage);
        }
        if (!examstaff.enums.ExamStatus.isInProgress(examSummary.getStatus())) {
            return fail("Kỳ thi \"" + examSummary.getExamName()
                    + "\" chưa ở trạng thái đang diễn ra (hiện tại: " + examSummary.getStatus() + ").");
        }
        // Mutate
        if (!examDAO.updateStatus(examId, examstaff.enums.ExamStatus.TAM_DUNG.getDisplayName())) {
            return fail("Không cập nhật được trạng thái tạm dừng kỳ thi. Vui lòng thử lại.");
        }
        // Result
        return ServiceResult.ok(label, okMessage);
    }

    /**
     * Tiếp tục kỳ thi sau khi tạm dừng.
     * @param examId mã kỳ thi
     * @return kết quả thành công/thất bại kèm thông báo
     */
    @Override
    public ServiceResult<String> resumeExam(int examId) {
        // Load
        ExamSummaryDTO examSummary = examDAO.getById(examId);
        // Validate
        if (examSummary == null) {
            return fail("Không tìm thấy kỳ thi.");
        }
        String label = buildExamLabel(examSummary);
        String okMessage = "Đã tiếp tục kỳ thi \"" + label + "\". Sát hạch viên có thể đăng nhập lại.";
        if (examstaff.enums.ExamStatus.isInProgress(examSummary.getStatus())) {
            return ServiceResult.ok(label, okMessage);
        }
        if (!examstaff.enums.ExamStatus.isPaused(examSummary.getStatus())) {
            return fail("Kỳ thi \"" + examSummary.getExamName()
                    + "\" không ở trạng thái tạm dừng (hiện tại: " + examSummary.getStatus() + ").");
        }
        // Mutate
        if (!examDAO.updateStatus(examId, examstaff.enums.ExamStatus.DANG_DIEN_RA.getDisplayName())) {
            return fail("Không cập nhật được trạng thái tiếp tục kỳ thi. Vui lòng thử lại.");
        }
        // Result
        return ServiceResult.ok(label, okMessage);
    }

    @Override
    public ServiceResult<String> generateExamPassword(int examId) {
        ExamSummaryDTO examSummary = examDAO.getById(examId);
        if (examSummary == null) {
            return fail("Không tìm thấy kỳ thi.");
        }
        String otp = randomSixDigitOtp();
        if (!examDAO.updateExamPassword(examId, otp)) {
            return fail("Không lưu được mật khẩu máy thi. Vui lòng thử lại.");
        }
        String label = buildExamLabel(examSummary);
        String codeHint = examSummary.getExamCode() != null && !examSummary.getExamCode().isBlank()
                ? examSummary.getExamCode().trim()
                : String.valueOf(examId);
        String message = "Mật khẩu máy thi kỳ \"" + label + "\": " + otp
                + " (mã kỳ: " + codeHint + " — chỉ cán bộ biết, nhập tại màn hình mở ca thi).";
        return ServiceResult.ok(otp, message);
    }

    private static String randomSixDigitOtp() {
        int value = 100_000 + new SecureRandom().nextInt(900_000);
        return String.valueOf(value);
    }
}
