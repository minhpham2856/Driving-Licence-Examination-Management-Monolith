package examstaff.service.impl.support.view;
import examstaff.service.impl.support.shared.ExamStaffExamRules;
import examstaff.service.impl.support.call.CandidateQueueServiceImpl;
import examstaff.service.impl.support.shared.ExamStaffExamQueryServiceImpl;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffPageContext;
import examstaff.dto.ExamStaffPageCommand;
import examstaff.dto.ExamStaffPickerViewDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Implementation: dựng context / picker trang Exam Staff. */
public class ExamStaffPageServiceImpl {

    private final ExamStaffExamQueryServiceImpl examQuery;
    private final CandidateQueueServiceImpl queueService;

    /** Wiring mặc định khi không inject từ composition root. */
    public ExamStaffPageServiceImpl() {
        this(new ExamStaffExamQueryServiceImpl(), new CandidateQueueServiceImpl());
    }

    /** Inject dependencies cho unit test / composition root. */
    public ExamStaffPageServiceImpl(ExamStaffExamQueryServiceImpl examQuery,
            CandidateQueueServiceImpl queueService) {
        this.examQuery = examQuery;
        this.queueService = queueService;
    }

    /**
     * Lấy toàn bộ kỳ thi cho trang staff.
     *
     * @return danh sách kỳ thi
     */
    public List<ExamSummaryDTO> listAllExams() {
        return examQuery.listAllExams();
    }

    /**
     * Tìm kỳ thi trong danh sách đã tải.
     *
     * @param examId   mã kỳ thi
     * @param allExams danh sách nguồn
     * @return kỳ thi khớp, hoặc null
     */
    public ExamSummaryDTO findExamById(int examId, List<ExamSummaryDTO> allExams) {
        // Validate
        if (examId <= 0) {
            return null;
        }
        // Load: ưu tiên danh sách đã tải
        ExamSummaryDTO found = ExamStaffExamRules.findExamById(allExams, examId);
        if (found != null) {
            return found;
        }
        // Result: fallback DAO; lỗi → null
        try {
            return examQuery.findByExamId(examId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Chọn kỳ đại diện (representative) trong nhóm cùng ngày / ngữ cảnh.
     *
     * @param allExams danh sách kỳ
     * @param examId   mã kỳ tham chiếu
     * @return kỳ đại diện
     */
    public ExamSummaryDTO representativeExam(List<ExamSummaryDTO> allExams, int examId) {
        // Validate
        if (examId <= 0) {
            return null;
        }
        // Load: kỳ cùng ngữ cảnh trong list
        List<ExamSummaryDTO> dayExams = ExamStaffExamRules.examsForExam(allExams, examId);
        if (!dayExams.isEmpty()) {
            return dayExams.get(0);
        }
        // Result: fallback kỳ chính qua DAO
        int primaryId = resolvePrimaryExamId(allExams, examId);
        if (primaryId > 0) {
            return examQuery.findByExamId(primaryId);
        }
        return null;
    }

    /**
     * Xác định mã kỳ chính để hiển thị / thao tác.
     *
     * @param allExams danh sách kỳ
     * @param examId   mã kỳ tham chiếu
     * @return mã kỳ chính
     */
    public int resolvePrimaryExamId(List<ExamSummaryDTO> allExams, int examId) {
        return ExamStaffExamRules.resolvePrimaryExamId(allExams, examId);
    }

    /**
     * Chọn mã kỳ mặc định khi chưa có lựa chọn.
     *
     * @param allExams danh sách kỳ
     * @return mã kỳ mặc định, hoặc 0 nếu danh sách rỗng
     */
    public int resolveDefaultExamId(List<ExamSummaryDTO> allExams) {
        ExamSummaryDTO first = firstPickerOption(allExams);
        if (first == null) {
            return 0;
        }
        return first.getId() > 0 ? first.getId() : first.getExamId();
    }

    /**
     * Xây dựng dữ liệu UI chọn kỳ thi (picker).
     *
     * @param allExams  danh sách kỳ
     * @param examId    mã kỳ đang chọn
     * @param urlExamId mã kỳ từ URL
     * @return view picker
     */
    public ExamStaffPickerViewDTO buildPickerView(List<ExamSummaryDTO> allExams, int examId, int urlExamId) {
        ExamStaffPickerViewDTO view = new ExamStaffPickerViewDTO();
        // Load: danh sách kỳ + option sidebar
        if (allExams == null || allExams.isEmpty()) {
            allExams = listAllExams();
        }
        List<ExamSummaryDTO> options = sortExamDaysForSidebar(buildExamOptions(allExams));
        view.setExamOptions(options);
        view.setAllExams(allExams);

        // Mutate: xác định kỳ hiện tại (URL → examId → đại diện → mặc định)
        ExamSummaryDTO current = null;
        if (urlExamId > 0) {
            current = findExamById(urlExamId, allExams);
            if (current != null) {
                examId = current.getId() > 0 ? current.getId() : current.getExamId();
                view.setSelectedExamId(urlExamId);
            }
        } else if (examId > 0) {
            current = findExamById(examId, allExams);
            view.setSelectedExamId(examId);
        }
        if (current == null && examId > 0) {
            current = representativeExam(allExams, examId);
        }
        if (current == null && !options.isEmpty()) {
            current = options.get(0);
            examId = current.getId() > 0 ? current.getId() : current.getExamId();
        }
        view.setCurrentExam(current);
        view.setExamId(examId);

        // Result: mã kỳ “committed” cho picker
        int committedExamId = 0;
        if (urlExamId > 0 && current != null) {
            committedExamId = urlExamId;
        } else {
            committedExamId = resolvePickerOptionExamId(options, examId);
        }
        if (committedExamId > 0) {
            view.setPickerCommittedExamId(committedExamId);
        } else if (examId > 0) {
            view.setPickerCommittedExamId(examId);
        }
        return view;
    }

    /**
     * Chuẩn bị toàn bộ ngữ cảnh trang staff từ input.
     *
     * @param input dữ liệu chuẩn bị trang
     * @return ngữ cảnh trang (kỳ thi, hàng đợi, …)
     */
    public ExamStaffPageContext preparePageContext(ExamStaffPageCommand input) {
        ExamStaffPageContext ctx = new ExamStaffPageContext();
        // Validate
        if (input == null) {
            return ctx;
        }

        // Load: danh sách kỳ
        List<ExamSummaryDTO> allExams = input.getAllExams();
        if (allExams == null || allExams.isEmpty()) {
            allExams = listAllExams();
        }
        ctx.setAllExams(allExams);

        // Mutate: resolve examId + picker, ưu tiên URL/selected
        int examId = resolveExamId(input, allExams);

        ExamStaffPickerViewDTO picker = buildPickerView(allExams, examId, input.getUrlExamId());
        examId = picker.getExamId();
        if (input.getUrlExamId() > 0) {
            examId = input.getUrlExamId();
        } else if (picker.getSelectedExamId() != null && picker.getSelectedExamId() > 0) {
            examId = picker.getSelectedExamId();
        }
        if (examId <= 0) {
            examId = resolvePrimaryExamId(allExams, picker.getExamId());
        }

        if (examId <= 0 && picker.getExamOptions() != null && !picker.getExamOptions().isEmpty()) {
            ExamSummaryDTO first = picker.getExamOptions().get(0);
            examId = first.getId() > 0 ? first.getId() : first.getExamId();
        }

        ctx.setExamId(examId);
        ctx.setPickerView(picker);

        // Result: gắn hàng đợi thí sinh theo kỳ
        List<ExamRegistrationDTO> candidates = resolveCandidates(input, examId, allExams);
        ctx.setCandidates(candidates);
        return ctx;
    }

    /**
     * Tải hoặc lấy cache hàng đợi thí sinh theo input trang.
     *
     * @param input    lệnh trang (cờ load + cache)
     * @param examId   mã kỳ đang xem
     * @param allExams danh sách kỳ nguồn
     * @return hàng đợi đầy đủ, hoặc list rỗng nếu không tải
     */
    private List<ExamRegistrationDTO> resolveCandidates(ExamStaffPageCommand input,
            int examId, List<ExamSummaryDTO> allExams) {
        // Validate: không yêu cầu tải → trả cache nếu khớp kỳ
        if (!input.isLoadCandidates()) {
            if (input.getCachedQueue() != null
                    && input.getLoadedExamId() != null && input.getLoadedExamId() == examId) {
                return input.getCachedQueue();
            }
            return new ArrayList<>();
        }

        // Load + Result: refresh queue qua CandidateQueueService
        ExamStaffPageCommand refresh = new ExamStaffPageCommand();
        refresh.setExamId(examId);
        refresh.setWebRoot(input.getWebRoot());
        refresh.setAllExams(allExams);
        refresh.setSelectedExamId(input.getSelectedExamId());
        refresh.setCallQueueOrder(input.getCallQueueOrder());
        refresh.setCallQueueOrderExamId(input.getCallQueueOrderExamId());
        CandidateQueueSnapshotDTO snapshot = queueService.refreshQueue(refresh);
        return snapshot.getFullQueue();
    }

    /**
     * Chọn examId từ URL / selected / tham số chuỗi / mặc định.
     *
     * @param input    lệnh trang
     * @param allExams danh sách kỳ
     * @return mã kỳ hợp lệ, hoặc 0
     */
    private int resolveExamId(ExamStaffPageCommand input, List<ExamSummaryDTO> allExams) {
        int examId = input.getUrlExamId();
        if (examId > 0) {
            ExamSummaryDTO picked = findExamById(examId, allExams);
            if (picked != null) {
                return picked.getId() > 0 ? picked.getId() : picked.getExamId();
            }
            return examId;
        }
        if (input.getSelectedExamId() != null && input.getSelectedExamId() > 0) {
            return input.getSelectedExamId();
        }
        int parsed = parseExamIdParam(input.getExamIdParam());
        if (parsed > 0) {
            return parsed;
        }
        return resolveDefaultExamId(allExams);
    }

    /**
     * Parse tham số examId dạng chuỗi.
     *
     * @param examIdParam chuỗi từ request (có thể blank)
     * @return số dương, hoặc 0 nếu không hợp lệ
     */
    private static int parseExamIdParam(String examIdParam) {
        if (examIdParam == null || examIdParam.isBlank()) {
            return 0;
        }
        try {
            int parsed = Integer.parseInt(examIdParam.trim());
            return parsed > 0 ? parsed : 0;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /**
     * Gom option kỳ thi (unique theo id) cho picker.
     *
     * @param allExams danh sách nguồn
     * @return danh sách option không trùng key
     */
    private List<ExamSummaryDTO> buildExamOptions(List<ExamSummaryDTO> allExams) {
        LinkedHashMap<Integer, ExamSummaryDTO> examOptionMap = new LinkedHashMap<>();
        if (allExams != null) {
            for (ExamSummaryDTO s : allExams) {
                int key = s.getId() > 0 ? s.getId() : s.getExamId();
                if (key <= 0 || examOptionMap.containsKey(key)) {
                    continue;
                }
                examOptionMap.put(key, s);
            }
        }
        return new ArrayList<>(examOptionMap.values());
    }

    /**
     * Khớp examId với option picker.
     *
     * @param options danh sách option
     * @param examId  mã cần khớp
     * @return id option khớp, hoặc 0
     */
    private static int resolvePickerOptionExamId(List<ExamSummaryDTO> options, int examId) {
        if (options == null || examId <= 0) {
            return 0;
        }
        for (ExamSummaryDTO opt : options) {
            if (opt.getExamId() == examId || opt.getId() == examId) {
                return opt.getId() > 0 ? opt.getId() : opt.getExamId();
            }
        }
        return 0;
    }

    /**
     * Sắp xếp danh sách kỳ thi hiển thị sidebar.
     *
     * @param options danh sách gốc
     * @return bản đã sắp theo quy tắc sidebar
     */
    private static List<ExamSummaryDTO> sortExamDaysForSidebar(List<ExamSummaryDTO> options) {
        return ExamStaffExamRules.sortExamDaysForSidebar(options);
    }

    /**
     * Option đầu tiên sau khi sort (để chọn kỳ mặc định).
     *
     * @param allExams danh sách kỳ nguồn
     * @return option đầu, hoặc null nếu rỗng
     */
    private ExamSummaryDTO firstPickerOption(List<ExamSummaryDTO> allExams) {
        List<ExamSummaryDTO> options = sortExamDaysForSidebar(buildExamOptions(allExams));
        return options.isEmpty() ? null : options.get(0);
    }
}
