package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExaminerAllocationActionResultDTO;
import examstaff.dto.ExaminerAllocationViewDTO;
import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.ServiceResult;
import examstaff.dto.UserDTO;
import shared.model.ExamArea;

import java.util.List;
import java.util.Map;

/**
 * Facade phân công sát hạch viên (SHV) vào khu vực thi theo kỳ.
 *
 * Hai tầng API:
 * - <b>Persistence thô</b> — assignExaminer(ExaminerSlotDTO),
 *       removeAssignment, getAssignmentsByExamId (DAO trực tiếp)
 * - <b>Desk API</b> — buildAllocationView, assignExaminer(...),
 *       removeExaminer (validate + ghi + ServiceResult cho UI)
 * Truy vấn hỗ trợ: getActiveExaminers, getAvailableAreasForExam,
 * buildExaminerMap, getExamById, getAreaById.
 */
public interface ExaminerAssignService {

    /**
     * Lấy tóm tắt kỳ thi theo mã.
     * @param examId mã kỳ thi
     * @return ExamSummaryDTO hoặc null
     */
    ExamSummaryDTO getExamById(int examId);

    /**
     * Lấy khu vực thi theo mã.
     * @param id mã khu vực
     * @return khu vực hoặc null
     */
    ExamArea getAreaById(int id);

    /**
     * Danh sách sát hạch viên đang hoạt động.
     * @return danh sách user SHV
     */
    List<UserDTO> getActiveExaminers();

    /**
     * Các khu vực còn trống / khả dụng để phân công trong kỳ.
     * @param examId mã kỳ thi
     * @return danh sách khu vực
     */
    List<ExamArea> getAvailableAreasForExam(int examId);

    /**
     * Các slot phân công SHV theo kỳ thi.
     * @param examId mã kỳ thi
     * @return danh sách slot
     */
    List<ExaminerSlotDTO> getAssignmentsByExamId(int examId);

    /**
     * Gán SHV vào slot (API persistence thô).
     * @param slot thông tin slot
     * @return true nếu thành công
     */
    boolean assignExaminer(ExaminerSlotDTO slot);

    /**
     * Gỡ phân công theo khóa slot.
     * @param slotKey khóa slot
     * @return true nếu thành công
     */
    boolean removeAssignment(String slotKey);

    /**
     * Ghép view màn phân công SHV (desk).
     * @param examId         mã kỳ thi
     * @param fallbackExamId mã kỳ dự phòng
     * @param allExams       danh sách kỳ
     * @return DTO view phân công
     */
    ExaminerAllocationViewDTO buildAllocationView(int examId, int fallbackExamId, List<ExamSummaryDTO> allExams);

    /**
     * Map mã user → thông tin sát hạch viên (lookup UI).
     * @return map userId → UserDTO
     */
    Map<Integer, UserDTO> buildExaminerMap();

    /**
     * Phân công SHV qua desk API (validate + ghi + kết quả UI).
     * @param targetExamId   mã kỳ thi đích
     * @param areaId         mã khu vực
     * @param examinerUserId mã user SHV
     * @param staffId        mã nhân viên thao tác
     * @return ServiceResult kèm kết quả
     */
    ServiceResult<ExaminerAllocationActionResultDTO> assignExaminer(int targetExamId, int areaId,
            int examinerUserId, int staffId);

    /**
     * Gỡ SHV qua desk API theo khóa slot.
     * @param slotKey khóa slot
     * @return ServiceResult kèm kết quả
     */
    ServiceResult<ExaminerAllocationActionResultDTO> removeExaminer(String slotKey);
}
