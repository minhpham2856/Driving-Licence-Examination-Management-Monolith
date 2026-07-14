package examstaff.service;

import examstaff.dto.AutoAllocateResultDTO;
import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.UserDTO;
import shared.model.ExamArea;

import java.util.List;

/**
 * Phân công giám khảo và tự động phân bổ phòng/sân cho thí sinh.
 */
public interface ExaminerAllocationService {

    /**
     * Lấy tóm tắt kỳ thi theo mã.
     *
     * @param examId mã kỳ thi
     * @return DTO kỳ thi hoặc null
     */
    ExamSummaryDTO getExamById(int examId);

    /**
     * Lấy khu vực thi theo mã.
     *
     * @param id mã ExamArea
     * @return khu vực hoặc null
     */
    ExamArea getAreaById(int id);

    /**
     * Danh sách giám khảo đang active để chọn phân công.
     *
     * @return danh sách UserDTO giám khảo
     */
    List<UserDTO> getActiveExaminers();

    /**
     * Các khu vực được phép dùng cho kỳ thi (gắn Exam_ExamArea; fallback theo loại nếu chưa gắn).
     *
     * @param examId mã kỳ thi
     * @return danh sách ExamArea
     */
    List<ExamArea> getAvailableAreasForExam(int examId);

    /**
     * Các slot phân công giám khảo hiện có của kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách slot
     */
    List<ExaminerSlotDTO> getAssignmentsByExamId(int examId);

    /**
     * Gán giám khảo vào một slot (kỳ + khu vực + phần thi).
     *
     * @param slot thông tin phân công
     * @return true nếu lưu thành công
     */
    boolean assignExaminer(ExaminerSlotDTO slot);

    /**
     * Gỡ phân công giám khảo theo khóa slot.
     *
     * @param slotKey khóa slot
     * @return true nếu gỡ thành công
     */
    boolean removeAssignment(String slotKey);

    /**
     * Tự động phân phòng lý thuyết cho toàn bộ thí sinh của kỳ.
     *
     * @param examId mã kỳ thi
     * @return kết quả phân bổ (số thành công / lỗi)
     */
    AutoAllocateResultDTO autoAllocateExam(int examId);

    /**
     * Tự động phân phòng cho một thí sinh cụ thể.
     *
     * @param examId         mã kỳ thi
     * @param registrationId mã đăng ký thí sinh
     * @return kết quả phân bổ
     */
    AutoAllocateResultDTO autoAllocateCandidate(int examId, int registrationId);

    /**
     * Phân sân thực hành cho thí sinh đã đỗ lý thuyết (cân bằng tải trên sân có sát hạch viên).
     *
     * @param examId mã kỳ thi
     * @return kết quả phân bổ thực hành
     */
    AutoAllocateResultDTO autoAllocatePracticalExam(int examId);
}
