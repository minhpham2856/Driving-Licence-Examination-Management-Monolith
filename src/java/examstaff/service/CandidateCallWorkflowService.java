package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateCallActionResultDTO;

import java.util.List;

/**
 * Thực thi từng action trên trang gọi thí sinh (startCall, absent, pause, đóng ca…).
 */
public interface CandidateCallWorkflowService {

    /**
     * Chạy một action gọi thí sinh và trả kết quả side-effect cho orchestrator trang.
     *
     * @param action           mã action ({@code startCall}, {@code absent}, {@code pauseShift}…)
     * @param sbd              SBD liên quan (nếu có)
     * @param fullQueue        hàng đợi đầy đủ (có thể bị sửa thứ tự in-place)
     * @param permanentAbsents danh sách đình chỉ trên session
     * @param boardExamId      kỳ thi trên bảng gọi
     * @param shiftEnded       ca đã đóng hay chưa
     * @param calledByStaffId  userId staff đang thao tác
     * @return kết quả: cập nhật callingSbd, alert, cờ reload/sync/promote…
     */
    CandidateCallActionResultDTO executeAction(String action, String sbd,
            List<ExamRegistrationDTO> fullQueue, List<ExamRegistrationDTO> permanentAbsents,
            int boardExamId, boolean shiftEnded, int calledByStaffId);

    /**
     * Ghi nhận lượt gọi thí sinh (audit CALL) khi promote SBD lên số đang gọi.
     *
     * @param activeQueue     hàng đợi còn pending
     * @param nextSbd         SBD được gọi
     * @param calledByStaffId userId staff
     */
    void recordCallingCandidate(List<ExamRegistrationDTO> activeQueue, String nextSbd, int calledByStaffId);
}
