package examstaff.service.impl;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExaminerAllocationActionResultDTO;
import examstaff.dto.ExaminerAllocationViewDTO;
import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.ServiceResult;
import examstaff.dto.UserDTO;
import examstaff.service.ExaminerAssignService;
import shared.enums.ErrorType;
import shared.model.ExamArea;

import java.util.List;
import java.util.Map;
import examstaff.service.impl.support.assign.ExaminerAllocationServiceImpl;
import examstaff.service.impl.support.assign.ExaminerAllocationDeskServiceImpl;

/** Facade phân công SHV: ủy quyền allocation + desk. */
public class ExaminerAssignServiceImpl implements ExaminerAssignService {

    private final ExaminerAllocationServiceImpl allocation;
    private final ExaminerAllocationDeskServiceImpl desk;

    /** Wiring mặc định. */
    public ExaminerAssignServiceImpl() {
        this.allocation = new ExaminerAllocationServiceImpl();
        this.desk = new ExaminerAllocationDeskServiceImpl(this.allocation);
    }

    /**
     * Inject dependencies (test / composition).
     *
     * @param allocation persistence / query phân công
     * @param desk       luồng desk API
     */
    public ExaminerAssignServiceImpl(ExaminerAllocationServiceImpl allocation,
            ExaminerAllocationDeskServiceImpl desk) {
        this.allocation = allocation;
        this.desk = desk;
    }

    /**
     * Ủy quyền sang {@link ExaminerAllocationServiceImpl#getExamById}.
     *
     * @param examId mã kỳ thi
     * @return tóm tắt kỳ thi
     */
    @Override
    public ExamSummaryDTO getExamById(int examId) {
        return allocation.getExamById(examId);
    }

    /**
     * Ủy quyền sang {@link ExaminerAllocationServiceImpl#getAreaById}.
     *
     * @param id mã khu vực
     * @return khu vực
     */
    @Override
    public ExamArea getAreaById(int id) {
        return allocation.getAreaById(id);
    }

    /**
     * Ủy quyền sang {@link ExaminerAllocationServiceImpl#getActiveExaminers}.
     *
     * @return danh sách SHV active
     */
    @Override
    public List<UserDTO> getActiveExaminers() {
        return allocation.getActiveExaminers();
    }

    /**
     * Ủy quyền sang {@link ExaminerAllocationServiceImpl#getAvailableAreasForExam}.
     *
     * @param examId mã kỳ thi
     * @return khu vực khả dụng
     */
    @Override
    public List<ExamArea> getAvailableAreasForExam(int examId) {
        return allocation.getAvailableAreasForExam(examId);
    }

    /**
     * Ủy quyền sang {@link ExaminerAllocationServiceImpl#getAssignmentsByExamId}.
     *
     * @param examId mã kỳ thi
     * @return danh sách slot
     */
    @Override
    public List<ExaminerSlotDTO> getAssignmentsByExamId(int examId) {
        return allocation.getAssignmentsByExamId(examId);
    }

    /**
     * Ủy quyền sang {@link ExaminerAllocationServiceImpl#assignExaminer}.
     *
     * @param slot thông tin slot
     * @return {@code true} nếu thành công
     */
    @Override
    public boolean assignExaminer(ExaminerSlotDTO slot) {
        return allocation.assignExaminer(slot);
    }

    /**
     * Ủy quyền sang {@link ExaminerAllocationServiceImpl#removeAssignment}.
     *
     * @param slotKey khóa slot
     * @return {@code true} nếu thành công
     */
    @Override
    public boolean removeAssignment(String slotKey) {
        return allocation.removeAssignment(slotKey);
    }

    /**
     * Ủy quyền sang {@link ExaminerAllocationDeskServiceImpl#buildAllocationView}.
     *
     * @param examId         mã kỳ thi
     * @param fallbackExamId mã kỳ dự phòng
     * @param allExams       danh sách kỳ
     * @return DTO view
     */
    @Override
    public ExaminerAllocationViewDTO buildAllocationView(int examId, int fallbackExamId,
            List<ExamSummaryDTO> allExams) {
        return desk.buildAllocationView(examId, fallbackExamId, allExams);
    }

    /**
     * Ủy quyền sang {@link ExaminerAllocationDeskServiceImpl#buildExaminerMap}.
     *
     * @return map userId → SHV
     */
    @Override
    public Map<Integer, UserDTO> buildExaminerMap() {
        return desk.buildExaminerMap();
    }

    /**
     * Phân công SHV qua desk rồi map kết quả → {@link ServiceResult}.
     *
     * @param targetExamId   mã kỳ đích
     * @param areaId         mã khu vực
     * @param examinerUserId mã user SHV
     * @param staffId        mã nhân viên thao tác
     * @return kết quả phân công
     */
    @Override
    public ServiceResult<ExaminerAllocationActionResultDTO> assignExaminer(int targetExamId, int areaId,
            int examinerUserId, int staffId) {
        // Mutate
        ExaminerAllocationActionResultDTO data = desk.assignExaminer(targetExamId, areaId, examinerUserId, staffId);
        // Result
        if (data != null && data.isSuccess()) {
            return ServiceResult.ok(data, data.getAlertMsg());
        }
        String message = data != null && data.getErrorMsg() != null
                ? data.getErrorMsg() : "Phân công thất bại.";
        return ServiceResult.fail(ErrorType.VALIDATION_FAILED, message, data);
    }

    /**
     * Gỡ SHV qua desk rồi map kết quả → {@link ServiceResult}.
     *
     * @param slotKey khóa slot
     * @return kết quả gỡ phân công
     */
    @Override
    public ServiceResult<ExaminerAllocationActionResultDTO> removeExaminer(String slotKey) {
        // Mutate
        ExaminerAllocationActionResultDTO data = desk.removeExaminer(slotKey);
        // Result
        if (data != null && data.isSuccess()) {
            return ServiceResult.ok(data, data.getAlertMsg());
        }
        String message = data != null && data.getErrorMsg() != null
                ? data.getErrorMsg() : "Gỡ phân công thất bại.";
        return ServiceResult.fail(ErrorType.VALIDATION_FAILED, message, data);
    }
}
