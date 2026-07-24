package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;

import java.util.List;

/**
 * Nghiệp vụ hàng đợi gọi thí sinh: làm mới, lọc, tìm và sắp xếp thứ tự gọi.
 */
public interface CandidateQueueService {

    /**
     * Làm mới hàng đợi theo ngữ cảnh kỳ thi / bộ lọc đầu vào.
     *
     * @param input ngữ cảnh refresh hàng đợi
     * @return snapshot hàng đợi sau khi làm mới
     */
    CandidateQueueSnapshotDTO refreshQueue(ExamStaffQueueRefreshInput input);

    /**
     * Dựng snapshot hàng đợi từ danh sách đã có.
     *
     * @param queue          hàng đợi nguồn
     * @param examId         mã kỳ ưu tiên
     * @param fallbackExamId mã kỳ dự phòng
     * @return snapshot phục vụ UI/gọi số
     */
    CandidateQueueSnapshotDTO buildSnapshot(List<ExamRegistrationDTO> queue, int examId, int fallbackExamId);

    /**
     * Lọc thí sinh còn chờ gọi (chưa hoàn tất thủ tục gọi theo quy tắc).
     *
     * @param queue hàng đợi đầy đủ
     * @return danh sách còn pending để gọi
     */
    List<ExamRegistrationDTO> filterPendingForCall(List<ExamRegistrationDTO> queue);

    /**
     * Tìm thí sinh trong hàng đợi theo số báo danh.
     *
     * @param queue hàng đợi
     * @param sbd   số báo danh
     * @return hồ sơ khớp, hoặc null
     */
    ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd);

    /**
     * Xác định SBD kế tiếp cần gọi sau một SBD cho trước.
     *
     * @param fullQueue hàng đợi đầy đủ
     * @param afterSbd  SBD tham chiếu (có thể null = lấy đầu danh sách gọi được)
     * @return SBD kế tiếp, hoặc null nếu hết
     */
    String resolveNextCallingSbd(List<ExamRegistrationDTO> fullQueue, String afterSbd);

    /**
     * Đưa thí sinh còn gọi được lên đầu hàng đợi.
     *
     * @param queue hàng đợi (sửa tại chỗ)
     * @param sbd   số báo danh
     * @return true nếu đã chuyển vị trí
     */
    boolean moveCallableCandidateToFront(List<ExamRegistrationDTO> queue, String sbd);

    /**
     * Đưa thí sinh còn gọi được xuống cuối hàng đợi.
     *
     * @param queue hàng đợi (sửa tại chỗ)
     * @param sbd   số báo danh
     * @return true nếu đã chuyển vị trí
     */
    boolean moveCallableCandidateToBottom(List<ExamRegistrationDTO> queue, String sbd);

    /**
     * Liệt kê thí sinh đang bị đình chỉ trong hàng đợi kỳ thi.
     *
     * @param queue hàng đợi
     * @return danh sách thí sinh suspended
     */
    List<ExamRegistrationDTO> listSuspendedInExam(List<ExamRegistrationDTO> queue);

    /**
     * Tìm thí sinh theo kỳ thi và SBD (ưu tiên examId, fallback nếu cần).
     *
     * @param examId         mã kỳ ưu tiên
     * @param fallbackExamId mã kỳ dự phòng
     * @param sbd            số báo danh
     * @return hồ sơ tìm được, hoặc null
     */
    ExamRegistrationDTO findByExam(int examId, int fallbackExamId, String sbd);

    /**
     * Xác định thí sinh đang gọi từ SBD (có thể nhảy sang SBD kế nếu đã hoàn tất thủ tục).
     *
     * @param callingSbd SBD đang gọi
     * @param queue      hàng đợi
     * @return hồ sơ đang gọi, hoặc null
     */
    ExamRegistrationDTO resolveCallingCandidate(String callingSbd, List<ExamRegistrationDTO> queue);

    /**
     * Đồng bộ SBD đang gọi giữa HTTP session và CallBoard (bỏ SBD đã xong/vắng/đình chỉ).
     *
     * @param httpCallingSbd SBD từ HTTP (ưu tiên)
     * @param callBoard      trạng thái bảng gọi (có thể null)
     * @param queue          hàng đợi
     * @return SBD đang gọi sau đồng bộ (có thể null)
     */
    String resolveSyncedCallingSbd(String httpCallingSbd, examstaff.dto.view.CallBoardState callBoard,
            List<ExamRegistrationDTO> queue);

    /**
     * Nếu SBD hiện tại đã xong/đình chỉ thì chuyển sang SBD kế tiếp còn gọi được.
     *
     * @param callingSbd     SBD đang gọi
     * @param candidateQueue hàng đợi
     * @return SBD sau khi advance (có thể null)
     */
    String advanceCallingIfDone(String callingSbd, List<ExamRegistrationDTO> candidateQueue);
}
