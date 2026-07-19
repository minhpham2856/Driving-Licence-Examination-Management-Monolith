package examstaff.service.impl.support.call;
import examstaff.service.impl.support.allocation.AllocationPassRules;
import examstaff.service.impl.support.shared.ExamStaffExamRules;
import examstaff.service.impl.support.shared.ExamStaffExamQueryServiceImpl;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffPageCommand;
import examstaff.dto.CallBoardState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Implementation: làm mới / lọc hàng đợi gọi thí sinh. */
public class CandidateQueueServiceImpl {

    private final CandidateQueueQueryServiceImpl queueQuery;
    private final ExamStaffExamQueryServiceImpl examQuery;

    /** Wiring mặc định khi không inject từ composition root. */
    public CandidateQueueServiceImpl() {
        this(new CandidateQueueQueryServiceImpl(), new ExamStaffExamQueryServiceImpl());
    }

    /**
     * Inject dependencies cho unit test / composition root.
     *
     * @param queueQuery truy vấn danh sách thí sinh kỳ thi
     * @param examQuery  danh sách / tìm kỳ thi
     */
    public CandidateQueueServiceImpl(CandidateQueueQueryServiceImpl queueQuery,
            ExamStaffExamQueryServiceImpl examQuery) {
        this.queueQuery = queueQuery;
        this.examQuery = examQuery;
    }

    /**
     * Làm mới hàng đợi theo ngữ cảnh kỳ thi / bộ lọc đầu vào.
     *
     * @param input ngữ cảnh refresh hàng đợi
     * @return snapshot hàng đợi sau khi làm mới
     */
    public CandidateQueueSnapshotDTO refreshQueue(ExamStaffPageCommand input) {
        CandidateQueueSnapshotDTO snapshot = new CandidateQueueSnapshotDTO();
        snapshot.setFullQueue(new ArrayList<>());
        // Validate
        if (input == null) {
            return snapshot;
        }

        // Load / resolve examId
        int examId = input.getExamId();
        List<ExamSummaryDTO> allExams = input.getAllExams();
        if (allExams == null || allExams.isEmpty()) {
            allExams = examQuery.listAllExams();
        }

        if (examId <= 0 && input.getSelectedExamId() != null && input.getSelectedExamId() > 0) {
            examId = input.getSelectedExamId();
        }
        if (examId <= 0 && allExams != null && !allExams.isEmpty()) {
            examId = ExamStaffExamRules.resolveDefaultExamId(allExams);
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

        // Mutate: tải → chuẩn hóa ảnh → cờ pass → reorder
        List<ExamRegistrationDTO> qList = loadCandidates(examId, examId, allExams);
        queueQuery.normalizePhotoPaths(qList);
        for (ExamRegistrationDTO c : qList) {
            AllocationPassRules.applyToCandidate(c);
        }
        qList = applyCallQueueOrder(input.getCallQueueOrder(), input.getCallQueueOrderExamId(), examId, qList);

        // Result
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

    /**
     * Thí sinh còn trong hàng đợi gọi được (ủy quyền {@link CallQueueRules}).
     *
     * @param candidate hồ sơ đăng ký
     * @return true nếu còn gọi được
     */
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
    public ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd) {
        return CallQueueRules.findBySbd(queue, sbd);
    }

    /**
     * SBD tiếp theo sau {@code afterSbd} trong hàng chờ gọi (wrap về đầu nếu cần).
     *
     * @param queue    hàng đợi nguồn
     * @param afterSbd SBD mốc (null/blank = lấy pending đầu)
     * @return SBD kế tiếp hoặc null
     */
    private String findNextPendingSbd(List<ExamRegistrationDTO> queue, String afterSbd) {
        // Validate
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        // Không có mốc → pending đầu
        if (afterSbd == null || afterSbd.isBlank()) {
            for (ExamRegistrationDTO c : queue) {
                if (isCallablePending(c)) {
                    return c.getSbd();
                }
            }
            return null;
        }
        // Quét sau afterSbd
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
        // Wrap về đầu (bỏ chính afterSbd)
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
    public boolean moveCallableCandidateToFront(List<ExamRegistrationDTO> queue, String sbd) {
        // Validate
        if (queue == null || sbd == null || sbd.isBlank()) {
            return false;
        }
        // Mutate: đưa phần tử callable khớp SBD lên index 0
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
    public boolean moveCallableCandidateToBottom(List<ExamRegistrationDTO> queue, String sbd) {
        // Validate
        if (queue == null || sbd == null || sbd.isBlank()) {
            return false;
        }
        // Mutate: đưa phần tử callable khớp SBD xuống cuối
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
    public List<ExamRegistrationDTO> listSuspendedInExam(List<ExamRegistrationDTO> queue) {
        return CallQueueRules.listSuspendedInExam(queue);
    }

    /**
     * Thí sinh đã xong thủ tục, mới nhất trước (theo presentMarkedAt rồi SBD).
     *
     * @param queue hàng đợi đầy đủ
     * @return danh sách đã lọc và sắp
     */
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
    public ExamRegistrationDTO findByExam(int examId, int fallbackExamId, String sbd) {
        // Validate
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        String trimmed = sbd.trim();
        try {
            // Load ưu tiên examId
            if (examId > 0) {
                ExamRegistrationDTO byExam = queueQuery.findByExamIdAndSbd(examId, trimmed);
                if (byExam != null) {
                    return byExam;
                }
            }
            // Fallback quét list theo fallbackExamId
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

    /**
     * Tải danh sách thí sinh theo examId (fallback nếu examId ≤ 0).
     *
     * @param examId         mã kỳ ưu tiên
     * @param fallbackExamId mã kỳ dự phòng
     * @param allExams       danh sách kỳ (giữ chữ ký API; không dùng trực tiếp)
     * @return danh sách (có thể rỗng, không null)
     */
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

    /**
     * Tìm {@link ExamSummaryDTO} theo id trong list hoặc DAO.
     *
     * @param examId   mã kỳ thi
     * @param allExams danh sách đã load sẵn (có thể null)
     * @return kỳ thi khớp, hoặc null
     */
    private ExamSummaryDTO resolveExam(int examId, List<ExamSummaryDTO> allExams) {
        ExamSummaryDTO found = ExamStaffExamRules.findExamById(allExams, examId);
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

    /**
     * Áp thứ tự gọi tùy chỉnh theo SBD (chỉ khi orderExamId khớp examId hiện tại).
     *
     * @param order       thứ tự SBD mong muốn
     * @param orderExamId kỳ thi của order đã lưu
     * @param examId      kỳ thi đang resolve
     * @param qList       hàng đợi gốc
     * @return hàng đã reorder hoặc {@code qList} nếu không áp dụng
     */
    private List<ExamRegistrationDTO> applyCallQueueOrder(List<String> order, Integer orderExamId,
            int examId, List<ExamRegistrationDTO> qList) {
        // Validate điều kiện áp dụng
        if (qList == null || qList.isEmpty() || order == null || order.isEmpty()
                || orderExamId == null || orderExamId != examId) {
            return qList;
        }
        // Mutate thứ tự theo order rồi ghép phần còn lại
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
    public ExamRegistrationDTO resolveCallingCandidate(String callingSbd, List<ExamRegistrationDTO> queue) {
        // Validate
        if (queue == null || callingSbd == null || callingSbd.isBlank()) {
            return null;
        }
        // Load / advance nếu đã xong thủ tục
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
    public String resolveSyncedCallingSbd(String httpCallingSbd, CallBoardState callBoard,
            List<ExamRegistrationDTO> queue) {
        // Load: ưu tiên HTTP, fallback board
        String boardCalling = callBoard != null ? callBoard.getCallingSbd() : null;
        String callingSbd = httpCallingSbd != null && !httpCallingSbd.isBlank()
                ? httpCallingSbd
                : boardCalling;
        // Mutate: bỏ SBD đã xong/vắng/đình chỉ
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
    public String advanceCallingIfDone(String callingSbd, List<ExamRegistrationDTO> candidateQueue) {
        // Validate: giữ nguyên nếu thiếu dữ liệu
        if (candidateQueue == null || callingSbd == null || callingSbd.isBlank()) {
            return callingSbd;
        }
        // Load hiện tại — còn gọi được thì giữ
        ExamRegistrationDTO current = findBySbd(candidateQueue, callingSbd);
        if (current != null && !current.isSuspended() && !current.isProcedureComplete()) {
            return callingSbd;
        }
        // Result: nhảy sang SBD kế
        return resolveNextCallingSbd(candidateQueue, callingSbd);
    }
}
