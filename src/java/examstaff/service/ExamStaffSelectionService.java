package examstaff.service;

import examstaff.dto.ExamSelectRequestDTO;
import examstaff.dto.ExamSelectResultDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffPageTransitionInput;
import examstaff.dto.ExamStaffPageTransitionStateDTO;
import examstaff.dto.ExamStaffSelectionResolveInput;
import examstaff.dto.ExamStaffSelectionStateDTO;

import java.util.List;

/**
 * Nghiệp vụ chọn / đồng bộ kỳ thi đang làm việc của nhân viên exam staff.
 */
public interface ExamStaffSelectionService {

    /**
     * Giải mã kỳ thi cần chọn từ dữ liệu đầu vào (URL, session, danh sách).
     *
     * @param input dữ liệu resolve lựa chọn kỳ thi
     * @return mã kỳ thi đã chọn, hoặc 0/sentinel nếu không hợp lệ
     */
    int resolveExamId(ExamStaffSelectionResolveInput input);

    /**
     * Đảm bảo có mã kỳ thi hợp lệ; chọn mặc định nếu đầu vào thiếu/không khớp.
     *
     * @param input dữ liệu resolve lựa chọn kỳ thi
     * @return mã kỳ thi chắc chắn dùng được trong ngữ cảnh hiện tại
     */
    int ensureExamId(ExamStaffSelectionResolveInput input);

    /**
     * Chọn kỳ thi từ tham số URL trong danh sách kỳ có sẵn.
     *
     * @param urlExamId mã kỳ trên URL
     * @param allExams  danh sách kỳ thi hiện có
     * @return mã kỳ hợp lệ, hoặc mặc định nếu URL không khớp
     */
    int resolveExamFromUrl(int urlExamId, List<ExamSummaryDTO> allExams);

    /**
     * Đồng bộ trạng thái chọn kỳ thi (đổi kỳ / giữ kỳ hiện tại).
     *
     * @param examId        mã kỳ muốn chọn
     * @param currentExamId mã kỳ đang chọn (có thể null)
     * @param allExams      danh sách kỳ thi
     * @return trạng thái chọn kỳ sau đồng bộ
     */
    ExamStaffSelectionStateDTO syncExamSelection(int examId, Integer currentExamId, List<ExamSummaryDTO> allExams);

    /**
     * Chuẩn bị chuyển trang theo ngữ cảnh chọn kỳ và hàng đợi.
     *
     * @param input dữ liệu chuyển trang
     * @return trạng thái trang sau khi chuẩn bị chuyển
     */
    ExamStaffPageTransitionStateDTO preparePageTransition(ExamStaffPageTransitionInput input);

    /**
     * Xác định kỳ thi đang active giữa URL, lựa chọn session và runtime.
     *
     * @param urlExamId            mã kỳ trên URL
     * @param selectedExamId       mã kỳ đã chọn (có thể null)
     * @param runtimeActiveExamId  mã kỳ active runtime (có thể null)
     * @return mã kỳ thi active ưu tiên theo quy tắc nghiệp vụ
     */
    int resolveActiveExamId(int urlExamId, Integer selectedExamId, Integer runtimeActiveExamId);

    /**
     * Xử lý yêu cầu chọn kỳ thi (endpoint select-exam).
     *
     * @param request thông tin chọn kỳ từ URL/session
     * @return kết quả chọn kỳ và cờ clear cache khi đổi kỳ
     */
    ExamSelectResultDTO processSelection(ExamSelectRequestDTO request);
}
