package examstaff.dto;

import examstaff.dto.ExamRegistrationDTO;

/**
 * Request thao tác phân bổ một thí sinh (Presentation → BLL).
 *
 * Vai trò:
 * Gom action (allocate / unallocate / …), regId, examId, profile và khu vực đích từ form allocation.
 *
 * Ai tạo / tiêu thụ:
 * AllocationServlet dựng request → AllocationActionServiceImpl xử lý →
 * AllocationActionResultDTO / ServiceResult.
 */
public class AllocationCandidateActionRequest {

    private String action;
    private int regId;
    private int examId;
    private ExamRegistrationDTO profile;
    private int areaId;

    /** Mã hành động phân bổ từ form (allocate, clear, move…). */
    public String getAction() {
        return action;
    }

    /** Gán mã hành động. */
    public void setAction(String action) {
        this.action = action;
    }

    /** Mã đăng ký / enrollment mục tiêu. */
    public int getRegId() {
        return regId;
    }

    /** Gán mã đăng ký thao tác. */
    public void setRegId(int regId) {
        this.regId = regId;
    }

    /** Kỳ thi đang thực hiện phân bổ. */
    public int getExamId() {
        return examId;
    }

    /** Gán mã kỳ thi. */
    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** Hồ sơ thí sinh đã resolve (tránh query lại nếu servlet đã load). */
    public ExamRegistrationDTO getProfile() {
        return profile;
    }

    /** Gán hồ sơ thí sinh kèm request. */
    public void setProfile(ExamRegistrationDTO profile) {
        this.profile = profile;
    }

    /** Id khu vực đích cần phân / chuyển. */
    public int getAreaId() {
        return areaId;
    }

    /** Gán id khu vực đích. */
    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }
}
