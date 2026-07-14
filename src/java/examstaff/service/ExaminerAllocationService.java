package examstaff.service;

import examstaff.dto.AutoAllocateResultDTO;
import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.UserDTO;
import shared.model.ExamArea;

import java.util.List;

/**
 * Phân công sát hạch viên / khu vực và tự động phân phòng cho thí sinh.
 */
public interface ExaminerAllocationService {

    /**
     * Lấy tóm tắt kỳ thi theo mã.
     *
     * @param examId mã kỳ thi
     * @return tóm tắt kỳ thi, hoặc null nếu không có
     */
    ExamSummaryDTO getExamById(int examId);

    /**
     * Lấy khu vực thi theo mã.
     *
     * @param id mã khu vực
     * @return khu vực, hoặc null nếu không có
     */
    ExamArea getAreaById(int id);

    /**
     * Lấy danh sách sát hạch viên đang active.
     *
     * @return danh sách sát hạch viên
     */
    List<UserDTO> getActiveExaminers();

    /**
     * Lấy các khu vực còn trống / khả dụng để phân cho kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách khu vực khả dụng
     */
    List<ExamArea> getAvailableAreasForExam(int examId);

    /**
     * Lấy các slot phân công sát hạch viên của kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách slot đã gán
     */
    List<ExaminerSlotDTO> getAssignmentsByExamId(int examId);

    /**
     * Gán sát hạch viên vào một slot/khu vực.
     *
     * @param slot thông tin slot cần gán
     * @return true nếu gán thành công
     */
    boolean assignExaminer(ExaminerSlotDTO slot);

    /**
     * Gỡ phân công theo khóa slot.
     *
     * @param slotKey khóa slot
     * @return true nếu gỡ thành công
     */
    boolean removeAssignment(String slotKey);

    /**
     * Tự động phân phòng/khu vực cho cả kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return kết quả auto-allocate
     */
    AutoAllocateResultDTO autoAllocateExam(int examId);

    /**
     * Tự động phân phòng cho một đăng ký thí sinh.
     *
     * @param examId         mã kỳ thi
     * @param registrationId mã đăng ký thí sinh
     * @return kết quả auto-allocate
     */
    AutoAllocateResultDTO autoAllocateCandidate(int examId, int registrationId);

    /**
     * Phân sân thực hành cho thí sinh đã đỗ lý thuyết (cân bằng tải trên sân có giám khảo).
     *
     * @param examId mã kỳ thi
     * @return kết quả auto-allocate thực hành
     */
    AutoAllocateResultDTO autoAllocatePracticalExam(int examId);
}
