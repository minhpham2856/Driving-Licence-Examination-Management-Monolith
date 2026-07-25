package examstaff.service;

import examstaff.dao.CallBoardDAO;
import examstaff.dto.CallBoardState;
import examstaff.dto.CandidateCallPageCommand;
import examstaff.dto.CandidateCallPageViewDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.PublicCallSnapshotDTO;

import java.util.List;

/**
 * Facade gọi thí sinh: trang candidate-call + CallBoard runtime + snapshot Public Call.
 *
 * Hai nguồn dữ liệu:
 * - <b>DB</b> — danh sách thí sinh / trạng thái đăng ký (qua queue services)
 * - <b>CallBoardDAO</b> — trạng thái runtime (calling, desk, pause) trong JVM
 * Pattern board: getState → CallBoardRules → saveState (+ setActiveExamId).
 */
public interface StaffCallService {

    /**
     * Chuẩn bị view trang gọi thí sinh từ command.
     * @param command lệnh trang gọi
     * @return DTO trang gọi
     */
    CandidateCallPageViewDTO preparePage(CandidateCallPageCommand command);

    /**
     * Đọc trạng thái CallBoard theo kỳ thi.
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @return trạng thái hoặc null
     */
    CallBoardState getBoardState(CallBoardDAO callBoardDAO, int examId);

    /**
     * Mã kỳ đang active trên CallBoard.
     * @param callBoardDAO DAO bảng gọi
     * @return mã kỳ hoặc null
     */
    Integer getActiveCallExamId(CallBoardDAO callBoardDAO);

    /**
     * Đồng bộ trạng thái bảng gọi (SBD đang gọi, hàng đợi, kết ca…).
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param callingSbd   SBD đang gọi
     * @param queue        hàng đợi
     * @param shiftEnded   đã kết ca
     */
    void syncBoard(CallBoardDAO callBoardDAO, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    /**
     * Đánh dấu bàn đang bận với thí sinh deskSbd.
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param deskSbd      SBD tại bàn
     * @param queue        hàng đợi
     * @param shiftEnded   đã kết ca
     */
    void occupyDesk(CallBoardDAO callBoardDAO, int examId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    /**
     * Giải phóng bàn và chuyển sang gọi SBD tiếp theo.
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param callingSbd   SBD đang / sẽ gọi
     * @param queue        hàng đợi
     * @param shiftEnded   đã kết ca
     */
    void releaseDeskAndCall(CallBoardDAO callBoardDAO, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    /**
     * Tạm dừng bảng gọi (giữ hàng đợi).
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param queue        hàng đợi
     */
    void pauseBoard(CallBoardDAO callBoardDAO, int examId, List<ExamRegistrationDTO> queue);

    /**
     * Tiếp tục bảng gọi sau tạm dừng / kết ca.
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     */
    void resumeBoard(CallBoardDAO callBoardDAO, int examId);

    /**
     * Load snapshot Public Call (màn hình chờ công khai).
     * @param examId mã kỳ thi
     * @param board  trạng thái CallBoard
     * @return snapshot public
     */
    PublicCallSnapshotDTO loadPublicSnapshot(int examId, CallBoardState board);

    /**
     * Xây snapshot hàng đợi nội bộ staff.
     * @param queue          hàng đợi
     * @param examId         mã kỳ thi
     * @param fallbackExamId mã kỳ dự phòng
     * @return snapshot
     */
    CandidateQueueSnapshotDTO buildSnapshot(List<ExamRegistrationDTO> queue, int examId, int fallbackExamId);

    /**
     * Làm mới snapshot hàng đợi theo command trang staff.
     * @param input lệnh trang
     * @return snapshot
     */
    CandidateQueueSnapshotDTO refreshQueue(examstaff.dto.ExamStaffPageCommand input);

    /**
     * Danh sách thí sinh UI hàng đợi (View DAO) — đường chuẩn cho staff UI.
     * @param examId mã kỳ thi
     * @return danh sách đăng ký theo kỳ
     */
    List<ExamRegistrationDTO> listQueueByExamId(int examId);

    /**
     * Đồng bộ SBD đang gọi giữa HTTP và CallBoard.
     * @param httpCallingSbd SBD từ request
     * @param callBoard      trạng thái bảng gọi
     * @param queue          hàng đợi
     * @return SBD đã sync
     */
    String resolveSyncedCallingSbd(String httpCallingSbd, CallBoardState callBoard,
            List<ExamRegistrationDTO> queue);

    /**
     * Resolve thí sinh đang được gọi theo SBD.
     * @param callingSbd số báo danh đang gọi
     * @param queue      hàng đợi
     * @return hồ sơ hoặc null
     */
    ExamRegistrationDTO resolveCallingCandidate(String callingSbd, List<ExamRegistrationDTO> queue);

    /**
     * Danh sách thí sinh bị đình chỉ trong hàng đợi.
     * @param queue hàng đợi
     * @return danh sách đình chỉ
     */
    List<ExamRegistrationDTO> listSuspendedInExam(List<ExamRegistrationDTO> queue);

    /**
     * SBD tiếp theo có thể gọi sau afterSbd.
     * @param fullQueue hàng đợi đầy đủ
     * @param afterSbd  SBD vừa xử lý
     * @return SBD tiếp theo hoặc null
     */
    String resolveNextCallingSbd(List<ExamRegistrationDTO> fullQueue, String afterSbd);

    /**
     * Tìm thí sinh trong hàng đợi theo SBD.
     * @param queue hàng đợi
     * @param sbd   số báo danh
     * @return hồ sơ hoặc null
     */
    ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd);

    /**
     * Đưa thí sinh có thể gọi lên đầu hàng đợi (mutate list).
     * @param queue hàng đợi (mutate)
     * @param sbd   số báo danh
     * @return true nếu đã chuyển
     */
    boolean moveCallableCandidateToFront(List<ExamRegistrationDTO> queue, String sbd);
}
