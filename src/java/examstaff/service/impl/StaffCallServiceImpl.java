package examstaff.service.impl;

import examstaff.dao.CallBoardDAO;
import examstaff.dao.impl.AuditLogDAOImpl;
import examstaff.dto.CallBoardState;
import examstaff.dto.CandidateCallPageCommand;
import examstaff.dto.CandidateCallPageViewDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.PublicCallSnapshotDTO;
import examstaff.service.StaffCallService;
import examstaff.service.impl.support.call.CallBoardRules;
import examstaff.service.impl.support.call.CallQueueRules;

import java.util.ArrayList;
import java.util.List;
import examstaff.service.impl.support.call.CandidateCallPageServiceImpl;
import examstaff.service.impl.support.call.CandidateQueueServiceImpl;
import examstaff.service.impl.support.call.CandidateQueueQueryServiceImpl;
import examstaff.service.impl.support.shared.ExamStaffExamQueryServiceImpl;
import examstaff.service.impl.support.call.CandidateCallWorkflowServiceImpl;
import examstaff.service.impl.support.call.CandidateAttendanceServiceImpl;

/**
 * Gộp trang gọi + CallBoard + Public Call snapshot + thao tác hàng đợi dùng chung.
 */
public class StaffCallServiceImpl implements StaffCallService {

    private final CandidateCallPageServiceImpl callPage;
    private final CandidateQueueServiceImpl queueService;
    private final CandidateQueueQueryServiceImpl queueQuery;
    private final ExamStaffExamQueryServiceImpl examQuery;

    /** Wiring mặc định (composition root). */
    public StaffCallServiceImpl() {
        this.queueService = new CandidateQueueServiceImpl();
        this.queueQuery = new CandidateQueueQueryServiceImpl();
        this.examQuery = new ExamStaffExamQueryServiceImpl();
        CandidateCallWorkflowServiceImpl workflow = new CandidateCallWorkflowServiceImpl(
                this.queueService, new AuditLogDAOImpl(), new CandidateAttendanceServiceImpl());
        this.callPage = new CandidateCallPageServiceImpl(workflow, this.queueService, this.examQuery);
    }

    /**
     * Inject callPage + queueService; tự tạo queueQuery / examQuery.
     *
     * @param callPage     dịch vụ trang gọi
     * @param queueService dịch vụ hàng đợi
     */
    public StaffCallServiceImpl(CandidateCallPageServiceImpl callPage, CandidateQueueServiceImpl queueService) {
        this.callPage = callPage;
        this.queueService = queueService;
        this.queueQuery = new CandidateQueueQueryServiceImpl();
        this.examQuery = new ExamStaffExamQueryServiceImpl();
    }

    /**
     * Ủy quyền sang {@link CandidateCallPageServiceImpl#preparePage}.
     *
     * @param command lệnh trang gọi
     * @return DTO trang gọi
     */
    @Override
    public CandidateCallPageViewDTO preparePage(CandidateCallPageCommand command) {
        return callPage.preparePage(command);
    }

    /**
     * Đọc trạng thái CallBoard; trả {@code null} nếu DAO/kỳ không hợp lệ.
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @return trạng thái hoặc {@code null}
     */
    @Override
    public CallBoardState getBoardState(CallBoardDAO callBoardDAO, int examId) {
        // Validate
        if (callBoardDAO == null || examId <= 0) {
            return null;
        }
        // Load
        return callBoardDAO.getState(examId);
    }

    /**
     * Lấy mã kỳ active trên CallBoard.
     *
     * @param callBoardDAO DAO bảng gọi
     * @return mã kỳ hoặc {@code null}
     */
    @Override
    public Integer getActiveCallExamId(CallBoardDAO callBoardDAO) {
        return callBoardDAO == null ? null : callBoardDAO.getActiveExamId();
    }

    /**
     * Đồng bộ bảng gọi: áp dụng rules rồi lưu + set kỳ active.
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param callingSbd   SBD đang gọi
     * @param queue        hàng đợi
     * @param shiftEnded   đã kết ca
     */
    @Override
    public void syncBoard(CallBoardDAO callBoardDAO, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        // Validate
        if (callBoardDAO == null || examId <= 0) {
            return;
        }
        // Mutate
        CallBoardState updated = CallBoardRules.syncBoard(
                callBoardDAO.getState(examId), examId, callingSbd, queue, shiftEnded);
        // Result
        callBoardDAO.saveState(examId, updated);
        callBoardDAO.setActiveExamId(examId);
    }

    /**
     * Đánh dấu bàn bận: validate → occupyDesk → lưu.
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param deskSbd      SBD tại bàn
     * @param queue        hàng đợi
     * @param shiftEnded   đã kết ca
     */
    @Override
    public void occupyDesk(CallBoardDAO callBoardDAO, int examId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        // Validate
        if (callBoardDAO == null || examId <= 0 || deskSbd == null || deskSbd.isBlank()) {
            return;
        }
        // Mutate
        CallBoardState updated = CallBoardRules.occupyDesk(
                callBoardDAO.getState(examId), examId, deskSbd, queue, shiftEnded);
        // Result
        callBoardDAO.saveState(examId, updated);
        callBoardDAO.setActiveExamId(examId);
    }

    /**
     * Giải phóng bàn và gọi tiếp: validate → releaseDeskAndCall → lưu.
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param callingSbd   SBD đang / sẽ gọi
     * @param queue        hàng đợi
     * @param shiftEnded   đã kết ca
     */
    @Override
    public void releaseDeskAndCall(CallBoardDAO callBoardDAO, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        // Validate
        if (callBoardDAO == null || examId <= 0) {
            return;
        }
        // Mutate
        CallBoardState updated = CallBoardRules.releaseDeskAndCall(
                callBoardDAO.getState(examId), examId, callingSbd, queue, shiftEnded);
        // Result
        callBoardDAO.saveState(examId, updated);
        callBoardDAO.setActiveExamId(examId);
    }

    /**
     * Tạm dừng bảng gọi: validate → pauseBoard → lưu.
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param queue        hàng đợi
     */
    @Override
    public void pauseBoard(CallBoardDAO callBoardDAO, int examId, List<ExamRegistrationDTO> queue) {
        // Validate
        if (callBoardDAO == null || examId <= 0) {
            return;
        }
        // Mutate
        CallBoardState updated = CallBoardRules.pauseBoard(
                callBoardDAO.getState(examId), examId, queue);
        // Result
        callBoardDAO.saveState(examId, updated);
        callBoardDAO.setActiveExamId(examId);
    }

    /**
     * Tiếp tục bảng gọi: xóa cờ tạm dừng / kết ca rồi lưu.
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     */
    @Override
    public void resumeBoard(CallBoardDAO callBoardDAO, int examId) {
        // Validate
        if (callBoardDAO == null || examId <= 0) {
            return;
        }
        // Load
        CallBoardState state = callBoardDAO.getState(examId);
        // Mutate / Result
        if (state != null) {
            state.setShiftEnded(false);
            state.setExamPaused(false);
            callBoardDAO.saveState(examId, state);
        }
    }

    /**
     * Ghép snapshot Public Call từ hàng đợi View DAO + trạng thái bảng gọi.
     *
     * @param examId      mã kỳ thi
     * @param webRootPath thư mục gốc web
     * @param board       trạng thái CallBoard
     * @return snapshot public
     */
    @Override
    public PublicCallSnapshotDTO loadPublicSnapshot(int examId, CallBoardState board) {
        // Validate / init
        PublicCallSnapshotDTO snapshot = new PublicCallSnapshotDTO();
        snapshot.setExamId(examId);
        snapshot.setWaitingQueue(new ArrayList<>());
        snapshot.setUpdatedAtMs(System.currentTimeMillis());
        if (examId <= 0) {
            return snapshot;
        }

        // Load
        List<ExamRegistrationDTO> queue = queueQuery.listByExamId(examId);
        queueQuery.normalizePhotoPaths(queue);
        if (board != null && board.getQueueOrderSbds() != null && !board.getQueueOrderSbds().isEmpty()) {
            queue = CallQueueRules.applyQueueOrder(queue, board.getQueueOrderSbds());
        }

        String callingSbd = board != null ? board.getCallingSbd() : null;
        String nextSbd = board != null ? board.getNextSbd() : null;
        boolean shiftEnded = board != null && board.isShiftEnded();
        boolean examPaused = board != null && board.isExamPaused();
        boolean deskBusy = board != null && board.isDeskBusy();
        String deskSbd = board != null ? board.getDeskSbd() : null;
        long updatedAtMs = board != null ? board.getUpdatedAtMs() : System.currentTimeMillis();

        // Resolve next SBD
        if ((nextSbd == null || nextSbd.isBlank()) && !shiftEnded && !examPaused && !deskBusy) {
            nextSbd = CallBoardRules.resolveNextSbd(board, queue);
        }
        if (nextSbd != null && !nextSbd.isBlank()) {
            if ((deskSbd != null && nextSbd.equals(deskSbd))
                    || (callingSbd != null && nextSbd.equals(callingSbd) && deskBusy)) {
                nextSbd = null;
            }
        }

        // Result
        snapshot.setCurrentExam(examQuery.findByExamId(examId));
        snapshot.setCallingCandidate(CallQueueRules.findBySbd(queue, callingSbd));
        snapshot.setNextCandidate(CallQueueRules.findBySbd(queue, nextSbd));
        snapshot.setWaitingQueue(CallQueueRules.listWaitingTop(queue, 10));
        snapshot.setCallingActive(snapshot.getCallingCandidate() != null && !shiftEnded && !examPaused);
        snapshot.setShiftEnded(shiftEnded);
        snapshot.setExamPaused(examPaused);
        snapshot.setUpdatedAtMs(updatedAtMs);
        snapshot.setDeskBusy(deskBusy);
        snapshot.setDeskSbd(deskSbd);
        return snapshot;
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#buildSnapshot}.
     *
     * @param queue          hàng đợi
     * @param examId         mã kỳ thi
     * @param fallbackExamId mã kỳ dự phòng
     * @return snapshot
     */
    @Override
    public CandidateQueueSnapshotDTO buildSnapshot(List<ExamRegistrationDTO> queue, int examId, int fallbackExamId) {
        return queueService.buildSnapshot(queue, examId, fallbackExamId);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#refreshQueue}.
     *
     * @param input lệnh trang
     * @return snapshot
     */
    @Override
    public CandidateQueueSnapshotDTO refreshQueue(examstaff.dto.ExamStaffPageCommand input) {
        return queueService.refreshQueue(input);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueQueryServiceImpl#listByExamId} (View DAO).
     *
     * @param examId mã kỳ thi
     * @return danh sách hàng đợi UI
     */
    @Override
    public List<ExamRegistrationDTO> listQueueByExamId(int examId) {
        return queueQuery.listByExamId(examId);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#resolveSyncedCallingSbd}.
     *
     * @param httpCallingSbd SBD từ request
     * @param callBoard      trạng thái bảng gọi
     * @param queue          hàng đợi
     * @return SBD đã sync
     */
    @Override
    public String resolveSyncedCallingSbd(String httpCallingSbd, CallBoardState callBoard,
            List<ExamRegistrationDTO> queue) {
        return queueService.resolveSyncedCallingSbd(httpCallingSbd, callBoard, queue);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#resolveCallingCandidate}.
     *
     * @param callingSbd SBD đang gọi
     * @param queue      hàng đợi
     * @return hồ sơ hoặc {@code null}
     */
    @Override
    public ExamRegistrationDTO resolveCallingCandidate(String callingSbd, List<ExamRegistrationDTO> queue) {
        return queueService.resolveCallingCandidate(callingSbd, queue);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#listSuspendedInExam}.
     *
     * @param queue hàng đợi
     * @return danh sách đình chỉ
     */
    @Override
    public List<ExamRegistrationDTO> listSuspendedInExam(List<ExamRegistrationDTO> queue) {
        return queueService.listSuspendedInExam(queue);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#resolveNextCallingSbd}.
     *
     * @param fullQueue hàng đợi đầy đủ
     * @param afterSbd  SBD vừa xử lý
     * @return SBD tiếp theo
     */
    @Override
    public String resolveNextCallingSbd(List<ExamRegistrationDTO> fullQueue, String afterSbd) {
        return queueService.resolveNextCallingSbd(fullQueue, afterSbd);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#findBySbd}.
     *
     * @param queue hàng đợi
     * @param sbd   số báo danh
     * @return hồ sơ hoặc {@code null}
     */
    @Override
    public ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd) {
        return queueService.findBySbd(queue, sbd);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#moveCallableCandidateToFront}.
     *
     * @param queue hàng đợi (mutate)
     * @param sbd   số báo danh
     * @return {@code true} nếu đã chuyển
     */
    @Override
    public boolean moveCallableCandidateToFront(List<ExamRegistrationDTO> queue, String sbd) {
        return queueService.moveCallableCandidateToFront(queue, sbd);
    }
}
