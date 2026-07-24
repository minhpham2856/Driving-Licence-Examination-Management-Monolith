package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffPageContextDTO;
import examstaff.dto.ExamStaffPagePrepareInput;
import examstaff.dto.ExamStaffPickerViewDTO;

import java.util.List;

/**
 * Ghép dữ liệu trang làm việc exam staff (picker kỳ thi, ngữ cảnh trang).
 */
public interface ExamStaffPageService {

    /**
     * Lấy toàn bộ kỳ thi cho trang staff.
     *
     * @return danh sách kỳ thi
     */
    List<ExamSummaryDTO> listAllExams();

    /**
     * Tìm kỳ thi trong danh sách đã tải.
     *
     * @param examId   mã kỳ thi
     * @param allExams danh sách nguồn
     * @return kỳ thi khớp, hoặc null
     */
    ExamSummaryDTO findExamById(int examId, List<ExamSummaryDTO> allExams);

    /**
     * Chọn kỳ đại diện (representative) trong nhóm cùng ngày / ngữ cảnh.
     *
     * @param allExams danh sách kỳ
     * @param examId   mã kỳ tham chiếu
     * @return kỳ đại diện
     */
    ExamSummaryDTO representativeExam(List<ExamSummaryDTO> allExams, int examId);

    /**
     * Xác định mã kỳ chính để hiển thị / thao tác.
     *
     * @param allExams danh sách kỳ
     * @param examId   mã kỳ tham chiếu
     * @return mã kỳ chính
     */
    int resolvePrimaryExamId(List<ExamSummaryDTO> allExams, int examId);

    /**
     * Chọn mã kỳ mặc định khi chưa có lựa chọn.
     *
     * @param allExams danh sách kỳ
     * @return mã kỳ mặc định, hoặc 0 nếu danh sách rỗng
     */
    int resolveDefaultExamId(List<ExamSummaryDTO> allExams);

    /**
     * Xây dựng dữ liệu UI chọn kỳ thi (picker).
     *
     * @param allExams  danh sách kỳ
     * @param examId    mã kỳ đang chọn
     * @param urlExamId mã kỳ từ URL
     * @return view picker
     */
    ExamStaffPickerViewDTO buildPickerView(List<ExamSummaryDTO> allExams, int examId, int urlExamId);

    /**
     * Chuẩn bị toàn bộ ngữ cảnh trang staff từ input.
     *
     * @param input dữ liệu chuẩn bị trang
     * @return ngữ cảnh trang (kỳ thi, hàng đợi, …)
     */
    ExamStaffPageContextDTO preparePageContext(ExamStaffPagePrepareInput input);
}
