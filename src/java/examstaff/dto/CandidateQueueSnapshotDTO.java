package examstaff.dto;

import examstaff.dto.ExamRegistrationDTO;

import java.util.List;

/**
 * Snapshot hàng chờ thí sinh tại một thời điểm (BLL → Presentation).
 *
 * Vai trò:
 * Tách full queue, active queue, danh sách đã xong thủ tục và examId đã resolve —
 * dùng khi refresh queue / chuẩn bị call / select-exam.
 *
 * Ai tạo / tiêu thụ:
 * {@code CandidateQueueServiceImpl} → page refresh, select-exam, call prep
 * ({@code ExamStaffPageSupport}, {@code CandidateCallPageServiceImpl}, …).
 */
public class CandidateQueueSnapshotDTO {

    private List<ExamRegistrationDTO> fullQueue;
    private List<ExamRegistrationDTO> activeQueue;
    private List<ExamRegistrationDTO> procedureDone;
    private int resolvedExamId;

    /** Toàn bộ hàng chờ sau merge / lọc theo kỳ. */
    public List<ExamRegistrationDTO> getFullQueue() {
        return fullQueue;
    }

    /** Gán full queue snapshot. */
    public void setFullQueue(List<ExamRegistrationDTO> fullQueue) {
        this.fullQueue = fullQueue;
    }

    /** Hàng chờ còn đủ điều kiện gọi / xử lý tiếp. */
    public List<ExamRegistrationDTO> getActiveQueue() {
        return activeQueue;
    }

    /** Gán active queue snapshot. */
    public void setActiveQueue(List<ExamRegistrationDTO> activeQueue) {
        this.activeQueue = activeQueue;
    }

    /** Thí sinh đã hoàn tất thủ tục bàn (ảnh + lệ phí). */
    public List<ExamRegistrationDTO> getProcedureDone() {
        return procedureDone;
    }

    /** Gán danh sách đã xong thủ tục. */
    public void setProcedureDone(List<ExamRegistrationDTO> procedureDone) {
        this.procedureDone = procedureDone;
    }

    /** ExamId đã resolve kèm snapshot (đối chiếu cache session). */
    public int getResolvedExamId() {
        return resolvedExamId;
    }

    /** Gán examId của snapshot. */
    public void setResolvedExamId(int resolvedExamId) {
        this.resolvedExamId = resolvedExamId;
    }
}
