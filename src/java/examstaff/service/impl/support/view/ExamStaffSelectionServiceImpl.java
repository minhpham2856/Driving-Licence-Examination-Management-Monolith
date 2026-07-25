package examstaff.service.impl.support.view;
import examstaff.service.impl.support.shared.ExamStaffExamRules;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffPageCommand;
import examstaff.dto.ExamTransitionResultDTO;
import examstaff.dto.ServiceResult;
import shared.enums.ErrorType;
import shared.enums.ExamStaffMessage;

import java.util.List;

/**
 * Resolve và đồng bộ lựa chọn kỳ thi cho mọi trang Exam Staff.
 * <p>
 * Wrap ExamStaffPageServiceImpl; xử lý ưu tiên URL → session selected → default,
 * cờ clear cache/procedure khi đổi kỳ. Trả examstaff.dto.ExamTransitionResultDTO
 * hoặc examstaff.dto.ServiceResult cho endpoint select-exam.
 *
 * Thứ tự ưu tiên examId:
 * - urlExamId từ request
 * - selectedExamId từ session
 * - defaultExamId / tham số chuỗi examIdParam
 * - Kỳ đầu tiên trong allExams (ExamStaffPageServiceImpl.resolveDefaultExamId)
 *
 * Chuyển trang / đổi kỳ:
 * - preparePageTransition — set persistSelection, clear candidate cache
 *       khi loadedExamId ≠ URL; clear procedure state khi đổi kỳ thật
 * - processSelection — endpoint POST select-exam; validate URL khớp list kỳ
 * - syncExamSelection — giữ currentExamId nếu còn hợp lệ trong ngữ cảnh ngày
 */
public class ExamStaffSelectionServiceImpl {

    private final ExamStaffPageServiceImpl pageService;

    /** Wiring mặc định khi không inject từ composition root. */
    public ExamStaffSelectionServiceImpl() {
        this(new ExamStaffPageServiceImpl());
    }

    /** Inject dependencies cho unit test / composition root. */
    public ExamStaffSelectionServiceImpl(ExamStaffPageServiceImpl pageService) {
        this.pageService = pageService;
    }

    /**
     * Giải mã kỳ thi cần chọn từ dữ liệu đầu vào (URL, session, danh sách).
     * @param input dữ liệu resolve lựa chọn kỳ thi
     * @return mã kỳ thi đã chọn, hoặc 0/sentinel nếu không hợp lệ
     */
    public int resolveExamId(ExamStaffPageCommand input) {
        // Validate
        if (input == null) {
            return 0;
        }
        // Load ưu tiên: URL → selected → default → param → list mặc định
        if (input.getUrlExamId() > 0) {
            return input.getUrlExamId();
        }
        Integer selectedExam = input.getSelectedExamId();
        if (selectedExam != null && selectedExam > 0) {
            return selectedExam;
        }
        if (input.getDefaultExamId() > 0) {
            return input.getDefaultExamId();
        }

        String examIdParam = input.getExamIdParam();
        if (examIdParam != null && !examIdParam.isBlank()) {
            try {
                int parsed = Integer.parseInt(examIdParam.trim());
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        List<ExamSummaryDTO> allExams = input.getAllExams();
        if (allExams != null && !allExams.isEmpty()) {
            return pageService.resolveDefaultExamId(allExams);
        }
        return 0;
    }

    /**
     * Đảm bảo có mã kỳ thi hợp lệ; chọn mặc định nếu đầu vào thiếu/không khớp.
     * @param input dữ liệu resolve lựa chọn kỳ thi
     * @return mã kỳ thi chắc chắn dùng được trong ngữ cảnh hiện tại
     */
    public int ensureExamId(ExamStaffPageCommand input) {
        // Validate / load: resolve trước; thiếu thì tải list và chọn mặc định
        int examId = resolveExamId(input);
        if (examId > 0) {
            return examId;
        }
        List<ExamSummaryDTO> allExams = input.getAllExams();
        if (allExams == null || allExams.isEmpty()) {
            allExams = pageService.listAllExams();
            input.setAllExams(allExams);
        }
        return pageService.resolveDefaultExamId(allExams);
    }

    /**
     * Chọn kỳ thi từ tham số URL trong danh sách kỳ có sẵn.
     * @param urlExamId mã kỳ trên URL
     * @param allExams  danh sách kỳ thi hiện có
     * @return mã kỳ hợp lệ, hoặc mặc định nếu URL không khớp
     */
    public int resolveExamFromUrl(int urlExamId, List<ExamSummaryDTO> allExams) {
        if (urlExamId <= 0) {
            return 0;
        }
        ExamSummaryDTO picked = pageService.findExamById(urlExamId, allExams);
        if (picked == null || picked.getExamId() <= 0) {
            return 0;
        }
        return picked.getExamId();
    }

    /**
     * Đồng bộ trạng thái chọn kỳ thi (đổi kỳ / giữ kỳ hiện tại).
     * @param examId        mã kỳ muốn chọn
     * @param currentExamId mã kỳ đang chọn (có thể null)
     * @param allExams      danh sách kỳ thi
     * @return trạng thái chọn kỳ sau đồng bộ
     */
    public ExamTransitionResultDTO syncExamSelection(int examId, Integer currentExamId,
            List<ExamSummaryDTO> allExams) {
        ExamTransitionResultDTO state = new ExamTransitionResultDTO();
        // Validate
        if (examId <= 0) {
            return state;
        }

        // Mutate: giữ current nếu hợp lệ; không thì resolve primary
        int resolved = currentExamId != null ? currentExamId : 0;
        if (resolved <= 0) {
            resolved = ExamStaffExamRules.resolvePrimaryExamId(allExams, examId);
        } else if (allExams != null) {
            ExamSummaryDTO picked = ExamStaffExamRules.findExamById(allExams, resolved);
            if (picked == null || picked.getExamId() != examId) {
                resolved = ExamStaffExamRules.resolvePrimaryExamId(allExams, examId);
            }
        }
        // Result
        state.setExamId(resolved > 0 ? resolved : examId);
        return state;
    }

    /**
     * Chuẩn bị chuyển trang theo ngữ cảnh chọn kỳ và hàng đợi.
     * @param input dữ liệu chuyển trang
     * @return trạng thái trang sau khi chuẩn bị chuyển
     */
    public ExamTransitionResultDTO preparePageTransition(ExamStaffPageCommand input) {
        ExamTransitionResultDTO state = new ExamTransitionResultDTO();
        // Validate: cần URL exam hợp lệ
        if (input == null || input.getUrlExamId() <= 0) {
            return state;
        }

        // Load kỳ từ URL
        List<ExamSummaryDTO> allExams = input.getAllExams();
        ExamSummaryDTO urlExam = pageService.findExamById(input.getUrlExamId(), allExams);
        if (urlExam == null || urlExam.getExamId() <= 0) {
            return state;
        }

        // Mutate: cờ persist + clear cache/procedure khi đổi kỳ
        state.setExamId(input.getUrlExamId());
        state.setPersistSelection(true);

        Integer loadedExamId = input.getLoadedExamId();
        if (loadedExamId == null || loadedExamId != input.getUrlExamId()) {
            state.setClearCandidateCache(true);
        }

        Integer previousExamId = input.getPreviousExamId();
        if (previousExamId != null && previousExamId > 0 && !previousExamId.equals(urlExam.getExamId())
                && !previousExamId.equals(input.getUrlExamId())) {
            state.setClearProcedureState(true);
        }

        // Result
        return state;
    }

    /**
     * Xác định kỳ thi đang active giữa URL, lựa chọn session và runtime.
     * @param urlExamId            mã kỳ trên URL
     * @param selectedExamId       mã kỳ đã chọn (có thể null)
     * @param runtimeActiveExamId  mã kỳ active runtime (có thể null)
     * @return mã kỳ thi active ưu tiên theo quy tắc nghiệp vụ
     */
    public int resolveActiveExamId(int urlExamId, Integer selectedExamId,
            Integer runtimeActiveExamId) {
        if (urlExamId > 0) {
            return urlExamId;
        }
        if (selectedExamId != null && selectedExamId > 0) {
            return selectedExamId;
        }
        if (runtimeActiveExamId != null && runtimeActiveExamId > 0) {
            return runtimeActiveExamId;
        }
        return 0;
    }

    /**
     * Xử lý yêu cầu chọn kỳ thi (endpoint select-exam).
     * @param request thông tin chọn kỳ từ URL/session (ExamStaffPageCommand)
     * @return kết quả chọn kỳ và cờ clear cache khi đổi kỳ
     */
    public ServiceResult<ExamTransitionResultDTO> processSelection(ExamStaffPageCommand request) {
        ExamTransitionResultDTO result = new ExamTransitionResultDTO();
        // Validate
        if (request == null) {
            result.setSuccess(false);
            result.setErrorMessage(ExamStaffMessage.EXAM_NOT_FOUND_PREFIX.formatExamNotFound(null));
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, result.getErrorMessage(), result);
        }
        result.setPreviousExamId(request.getPreviousExamId());

        // Load: danh sách kỳ + resolve từ URL
        List<ExamSummaryDTO> allExams = pageService.listAllExams();
        int urlExamId = request.getUrlExamId();
        int examId = resolveExamFromUrl(urlExamId, allExams);

        if (examId <= 0) {
            result.setSuccess(false);
            String param = urlExamId > 0 ? String.valueOf(urlExamId) : null;
            result.setErrorMessage(ExamStaffMessage.EXAM_NOT_FOUND_PREFIX.formatExamNotFound(param));
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, result.getErrorMessage(), result);
        }

        // Mutate: mã kỳ commit + cờ clear khi đổi kỳ
        int resolvedExamId = urlExamId > 0
                ? urlExamId
                : pageService.resolvePrimaryExamId(allExams, examId);

        result.setSuccess(true);
        result.setExamId(resolvedExamId > 0 ? resolvedExamId : examId);
        result.setNewExamId(result.getExamId());

        Integer previousExamId = request.getPreviousExamId();
        if (previousExamId != null && previousExamId > 0 && !previousExamId.equals(result.getExamId())) {
            // Presentation chỉ cần cờ này: clearProcedureStateOnExamChange đã xóa cache queue.
            result.setClearProcedureState(true);
        }
        // Result
        return ServiceResult.ok(result);
    }
}
