package examstaff.service.impl;

import examstaff.util.AllocationPassRules;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;
import examstaff.dto.CallBoardState;
import examstaff.service.CandidateQueueQueryService;
import examstaff.service.CandidateQueueService;
import examstaff.service.ExamStaffExamQueryService;
import examstaff.util.CallQueueRules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Implementation: làm mới / lọc hàng đợi gọi thí sinh. */
public class CandidateQueueServiceImpl implements CandidateQueueService {

    private final CandidateQueueQueryService queueQuery;
    private final ExamStaffExamQueryService examQuery;

    /** Wiring mặc định khi không inject từ composition root. */
    public CandidateQueueServiceImpl() {
        this(new CandidateQueueQueryServiceImpl(), new ExamStaffExamQueryServiceImpl());
    }

    /** Inject dependencies cho unit test / composition root. */
    public CandidateQueueServiceImpl(CandidateQueueQueryService queueQuery,
            ExamStaffExamQueryService examQuery) {
        this.queueQuery = queueQuery;
        this.examQuery = examQuery;
    }

    /**
     * Làm mới hàng đợi theo ngữ cảnh kỳ thi / bộ lọc đầu vào.
     *
     * @param input ngữ cảnh refresh hàng đợi
     * @return snapshot hàng đợi sau khi làm mới
     */
    @Override
    public CandidateQueueSnapshotDTO refreshQueue(ExamStaffQueueRefreshInput input) {
        CandidateQueueSnapshotDTO snapshot = new CandidateQueueSnapshotDTO();
        snapshot.setFullQueue(new ArrayList<>());
        if (input == null) {
            return snapshot;
        }

        int examId = input.getExamId();
        List<ExamSummaryDTO> allExams = input.getAllExams();
        if (allExams == null || allExams.isEmpty()) {
            allExams = examQuery.listAllExams();
        }

        if (examId <= 0 && input.getSelectedExamId() != null && input.getSelectedExamId() > 0) {
            examId = input.getSelectedExamId();
        }
        if (examId <= 0 && allExams != null && !allExams.isEmpty()) {
            examId = examstaff.util.ExamStaffExamRules.resolveDefaultExamId(allExams);
        }
        if (examId <= 0) {
            return snapshot;
        }

        Integer selected = input.getSelectedExamId();
        if (selected != null && selected > 0) {
            ExamSummaryDTO pickedExam = resolveExam(selected, allExams);
            if (pickedExam != null && (pickedExam.getExamId() == examId || pickedExam.getId() == examId)) {
                examId = selected;
            }
        }

        List<ExamRegistrationDTO> qList = loadCandidates(examId, examId, allExams);
        if (input.getWebRoot() != null) {
            queueQuery.normalizePhotoPaths(input.getWebRoot(), qList);
        }
        for (ExamRegistrationDTO c : qList) {
            AllocationPassRules.applyToCandidate(c);
        }
        qList = applyCallQueueOrder(input.getCallQueueOrder(), input.getCallQueueOrderExamId(), examId, qList);

        snapshot.setResolvedExamId(examId);
        return buildSnapshot(qList, examId, examId);
    }

    /**
     * Dựng snapshot hàng đợi từ danh sách đã có.
     *
     * @param queue          hàng đợi nguồn
     * @param examId         mã kỳ ưu tiên
     * @param fallbackExamId mã kỳ dự phòng
     * @return snapshot phục vụ UI/gọi số
     */
    @Override
    public CandidateQueueSnapshotDTO buildSnapshot(List<ExamRegistrationDTO> queue, int examId, int fallbackExamId) {
        CandidateQueueSnapshotDTO snapshot = new CandidateQueueSnapshotDTO();
        List<ExamRegistrationDTO> qList = queue != null ? queue : List.of();
        snapshot.setFullQueue(qList);
        snapshot.setActiveQueue(filterPendingForCall(qList));
        snapshot.setProcedureDone(listProcedureDoneNewestFirst(qList));
        snapshot.setResolvedExamId(examId > 0 ? examId : fallbackExamId);
        return snapshot;
    }

    /**
     * Lọc thí sinh còn chờ gọi (chưa hoàn tất thủ tục gọi theo quy tắc).
     *
     * @param queue hàng đợi đầy đủ
     * @return danh sách còn pending để gọi
     */
    @Override
    public List<ExamRegistrationDTO> filterPendingForCall(List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return new ArrayList<>();
        }
        List<ExamRegistrationDTO> active = new ArrayList<>();
        for (ExamRegistrationDTO c : queue) {
            if (isCallablePending(c)) {
                active.add(c);
            }
        }
        return active;
    }

    /** Thí sinh còn trong hàng đợi gọi được. */
    private boolean isCallablePending(ExamRegistrationDTO candidate) {
        return CallQueueRules.isCallablePending(candidate);
    }

    /**
     * Tìm thí sinh trong hàng đợi theo số báo danh.
     *
     * @param queue hàng đợi
     * @param sbd   số báo danh
     * @return hồ sơ khớp, hoặc null
     */
    @Override
    public ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd) {
        return CallQueueRules.findBySbd(queue, sbd);
    }

    /** SBD tiếp theo sau afterSbd trong hàng chờ gọi. */
    private String findNextPendingSbd(List<ExamRegistrationDTO> queue, String afterSbd) {
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        if (afterSbd == null || afterSbd.isBlank()) {
            for (ExamRegistrationDTO c : queue) {
                if (isCallablePending(c)) {
                    return c.getSbd();
                }
            }
            return null;
        }
        boolean passed = false;
        for (ExamRegistrationDTO c : queue) {
            if (!passed) {
                if (afterSbd.equals(c.getSbd())) {
                    passed = true;
                }
                continue;
            }
            if (isCallablePending(c)) {
                return c.getSbd();
            }
        }
        for (ExamRegistrationDTO c : queue) {
            if (afterSbd.equals(c.getSbd())) {
                continue;
            }
            if (isCallablePending(c)) {
                return c.getSbd();
            }
        }
        return null;
    }

    /**
     * Xác định SBD kế tiếp cần gọi sau một SBD cho trước.
     *
     * @param fullQueue hàng đợi đầy đủ
     * @param afterSbd  SBD tham chiếu (có thể null = lấy đầu danh sách gọi được)
     * @return SBD kế tiếp, hoặc null nếu hết
     */
    @Override
    public String resolveNextCallingSbd(List<ExamRegistrationDTO> fullQueue, String afterSbd) {
        List<ExamRegistrationDTO> active = filterPendingForCall(fullQueue);
        if (active.isEmpty()) {
            return null;
        }
        if (afterSbd == null || afterSbd.isBlank()) {
            return active.get(0).getSbd();
        }
        String next = findNextPendingSbd(active, afterSbd);
        return next;
    }

    /**
     * Đưa thí sinh còn gọi được lên đầu hàng đợi.
     *
     * @param queue hàng đợi (sửa tại chỗ)
     * @param sbd   số báo danh
     * @return true nếu đã chuyển vị trí
     */
    @Override
    public boolean moveCallableCandidateToFront(List<ExamRegistrationDTO> queue, String sbd) {
        if (queue == null || sbd == null || sbd.isBlank()) {
            return false;
        }
        for (int i = 0; i < queue.size(); i++) {
            ExamRegistrationDTO c = queue.get(i);
            if (!sbd.equals(c.getSbd()) || !isCallablePending(c)) {
                continue;
            }
            if (i == 0) {
                return true;
            }
            queue.remove(i);
            queue.add(0, c);
            return true;
        }
        return false;
    }

    /**
     * Đưa thí sinh còn gọi được xuống cuối hàng đợi.
     *
     * @param queue hàng đợi (sửa tại chỗ)
     * @param sbd   số báo danh
     * @return true nếu đã chuyển vị trí
     */
    @Override
    public boolean moveCallableCandidateToBottom(List<ExamRegistrationDTO> queue, String sbd) {
        if (queue == null || sbd == null || sbd.isBlank()) {
            return false;
        }
        for (int i = 0; i < queue.size(); i++) {
            ExamRegistrationDTO c = queue.get(i);
            if (!sbd.equals(c.getSbd()) || !isCallablePending(c)) {
                continue;
            }
            queue.remove(i);
            queue.add(c);
            return true;
        }
        return false;
    }

    /**
     * Liệt kê thí sinh đang bị đình chỉ trong hàng đợi kỳ thi.
     *
     * @param queue hàng đợi
     * @return danh sách thí sinh suspended
     */
    @Override
    public List<ExamRegistrationDTO> listSuspendedInExam(List<ExamRegistrationDTO> queue) {
        return CallQueueRules.listSuspendedInExam(queue);
    }

    /** Thí sinh đã xong thủ tục, mới nhất trước. */
    private List<ExamRegistrationDTO> listProcedureDoneNewestFirst(List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return List.of();
        }
        List<ExamRegistrationDTO> done = new ArrayList<>();
        for (ExamRegistrationDTO c : queue) {
            if (c.isProcedureComplete()) {
                done.add(c);
            }
        }
        done.sort(Comparator
                .comparing(ExamRegistrationDTO::getPresentMarkedAt,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ExamRegistrationDTO::getSbd));
        return done;
    }

    /**
     * Tìm thí sinh theo kỳ thi và SBD (ưu tiên examId, fallback nếu cần).
     *
     * @param examId         mã kỳ ưu tiên
     * @param fallbackExamId mã kỳ dự phòng
     * @param sbd            số báo danh
     * @return hồ sơ tìm được, hoặc null
     */
    @Override
    public ExamRegistrationDTO findByExam(int examId, int fallbackExamId, String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        String trimmed = sbd.trim();
        try {
            if (examId > 0) {
                ExamRegistrationDTO byExam = queueQuery.findByExamIdAndSbd(examId, trimmed);
                if (byExam != null) {
                    return byExam;
                }
            }
            if (fallbackExamId > 0) {
                for (ExamRegistrationDTO c : queueQuery.listByExamId(fallbackExamId)) {
                    if (trimmed.equals(c.getSbd())) {
                        return c;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Tải danh sách thí sinh theo examId. */
    private List<ExamRegistrationDTO> loadCandidates(int examId, int fallbackExamId, List<ExamSummaryDTO> allExams) {
        try {
            if (examId <= 0 && fallbackExamId > 0) {
                examId = fallbackExamId;
            }
            if (examId > 0) {
                return new ArrayList<>(queueQuery.listByExamId(examId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /** Tìm ExamSummaryDTO theo id trong list hoặc DAO. */
    private ExamSummaryDTO resolveExam(int examId, List<ExamSummaryDTO> allExams) {
        ExamSummaryDTO found = examstaff.util.ExamStaffExamRules.findExamById(allExams, examId);
        if (found != null) {
            return found;
        }
        try {
            return examQuery.findByExamId(examId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Áp thứ tự gọi tùy chỉnh theo SBD. */
    private List<ExamRegistrationDTO> applyCallQueueOrder(List<String> order, Integer orderExamId,
            int examId, List<ExamRegistrationDTO> qList) {
        if (qList == null || qList.isEmpty() || order == null || order.isEmpty()
                || orderExamId == null || orderExamId != examId) {
            return qList;
        }
        Map<String, ExamRegistrationDTO> bySbd = new LinkedHashMap<>();
        for (ExamRegistrationDTO c : qList) {
            if (c != null && c.getSbd() != null) {
                bySbd.put(c.getSbd(), c);
            }
        }
        List<ExamRegistrationDTO> reordered = new ArrayList<>();
        for (String sbd : order) {
            ExamRegistrationDTO c = bySbd.remove(sbd);
            if (c != null) {
                reordered.add(c);
            }
        }
        reordered.addAll(bySbd.values());
        return reordered;
    }

    /**
     * Xác định thí sinh đang gọi từ SBD (có thể nhảy sang SBD kế nếu đã hoàn tất thủ tục).
     *
     * @param callingSbd SBD đang gọi
     * @param queue      hàng đợi
     * @return hồ sơ đang gọi, hoặc null
     */
    @Override
    public ExamRegistrationDTO resolveCallingCandidate(String callingSbd, List<ExamRegistrationDTO> queue) {
        if (queue == null || callingSbd == null || callingSbd.isBlank()) {
            return null;
        }
        for (ExamRegistrationDTO c : queue) {
            if (!callingSbd.equals(c.getSbd())) {
                continue;
            }
            if (!c.isProcedureComplete()) {
                return c;
            }
            String nextSbd = resolveNextCallingSbd(queue, callingSbd);
            return nextSbd != null ? findBySbd(queue, nextSbd) : null;
        }
        return null;
    }

    /**
     * Đồng bộ SBD đang gọi giữa HTTP session và CallBoard (bỏ SBD đã xong/vắng/đình chỉ).
     *
     * @param httpCallingSbd SBD từ HTTP (ưu tiên)
     * @param callBoard      trạng thái bảng gọi (có thể null)
     * @param queue          hàng đợi
     * @return SBD đang gọi sau đồng bộ (có thể null)
     */
    @Override
    public String resolveSyncedCallingSbd(String httpCallingSbd, CallBoardState callBoard,
            List<ExamRegistrationDTO> queue) {
        String boardCalling = callBoard != null ? callBoard.getCallingSbd() : null;
        String callingSbd = httpCallingSbd != null && !httpCallingSbd.isBlank()
                ? httpCallingSbd
                : boardCalling;
        if (callingSbd != null && !callingSbd.isBlank() && queue != null) {
            ExamRegistrationDTO atDesk = findBySbd(queue, callingSbd);
            if (atDesk == null || atDesk.isProcedureComplete() || atDesk.isSuspended() || atDesk.isAbsent()) {
                callingSbd = resolveNextCallingSbd(queue, callingSbd);
            }
        }
        return callingSbd;
    }

    /**
     * Nếu SBD hiện tại đã xong/đình chỉ thì chuyển sang SBD kế tiếp còn gọi được.
     *
     * @param callingSbd     SBD đang gọi
     * @param candidateQueue hàng đợi
     * @return SBD sau khi advance (có thể null)
     */
    @Override
    public String advanceCallingIfDone(String callingSbd, List<ExamRegistrationDTO> candidateQueue) {
        if (candidateQueue == null || callingSbd == null || callingSbd.isBlank()) {
            return callingSbd;
        }
        ExamRegistrationDTO current = findBySbd(candidateQueue, callingSbd);
        if (current != null && !current.isSuspended() && !current.isProcedureComplete()) {
            return callingSbd;
        }
        return resolveNextCallingSbd(candidateQueue, callingSbd);
    }
}
