package examstaff.controller;

import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamStaffPageCommand;
import examstaff.dto.ExamStaffPageContext;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamTransitionResultDTO;
import examstaff.service.ExamStaffViewService;
import examstaff.service.impl.ExamStaffViewServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;

/**
 * Thin Presentation: chuẩn bị trang exam staff + chọn kỳ (HTTP ↔ ViewService ↔ binder).
 * Thay ExamStaffPageFacade / ExamStaffSelectionFacade.
 *
 * Vai trò:
 * Điểm vào chuẩn bị ngữ cảnh trang exam staff: xử lý transition đổi kỳ từ URL,
 * gọi ExamStaffViewService.preparePageContext, persist selection, bind picker và publish queue.
 * Cung cấp helper ensureExamId, resolveExamId, applyExamIdFromRequest.
 *
 * Luồng sử dụng:
 * - Servlet gọi prepareExamStaffPage(request, session, webRoot, loadCandidates, view)
 * - Nếu URL có examId → transition + clear cache/procedure theo cờ service
 * - Trả ExamStaffPageContext (examId, candidates, picker) cho servlet tiếp tục bind/action
 *
 * Ai gọi:
 * Hầu hết trang exam staff: DashboardServlet, CandidateCallServlet,
 * ProcedureServlet, ReportServlet, AllocationServlet, ExaminerAllocationServlet.
 */
public final class ExamStaffPageSupport {

    private static final ExamStaffViewService VIEW = new ExamStaffViewServiceImpl();

    /** Không khởi tạo. */
    private ExamStaffPageSupport() {
    }

    /**
     * Chuẩn bị trang exam staff (load candidates mặc định, view mặc định).
     * @see #prepareExamStaffPage(HttpServletRequest, HttpSession, String, boolean, ExamStaffViewService)
     */
    public static ExamStaffPageContext prepareExamStaffPage(HttpServletRequest request, HttpSession session,
            String webRoot) {
        return prepareExamStaffPage(request, session, webRoot, true, VIEW);
    }

    /**
     * Chuẩn bị trang exam staff với cờ load candidates (view mặc định).
     * @see #prepareExamStaffPage(HttpServletRequest, HttpSession, String, boolean, ExamStaffViewService)
     */
    public static ExamStaffPageContext prepareExamStaffPage(HttpServletRequest request, HttpSession session,
            String webRoot, boolean loadCandidates) {
        return prepareExamStaffPage(request, session, webRoot, loadCandidates, VIEW);
    }

    /**
     * Chuẩn bị ngữ cảnh trang exam staff cho servlet.
     * <p>
     * Luồng: UTF-8 → (nếu có examId URL) xử lý transition đổi kỳ → build command →
     * preparePageContext → persist selection → bind picker → publish queue.
     * @param request        request HTTP
     * @param session        session staff
     * @param webRoot        real path web root (ảnh/hồ sơ)
     * @param loadCandidates có nạp queue thí sinh hay không
     * @param view           ViewService (null → mặc định)
     * @return context (examId, candidates, picker, …)
     */
    public static ExamStaffPageContext prepareExamStaffPage(HttpServletRequest request, HttpSession session,
            String webRoot, boolean loadCandidates, ExamStaffViewService view) {
        ExamStaffViewService v = view != null ? view : VIEW;
        applyUtf8Request(request);
        int urlExamId = ExamStaffHttpSupport.parseExamIdParam(request);

        // Đổi kỳ từ URL: clear cache/procedure theo cờ transition rồi persist
        if (urlExamId > 0 && session != null) {
            ExamTransitionResultDTO transition = v.preparePageTransition(
                    buildPageTransitionInput(session, urlExamId, v));
            if (transition.isClearCandidateCache()) {
                ExamStaffPageBinder.clearCandidateCache(session);
            }
            if (transition.isClearProcedureState()) {
                ExamStaffPageBinder.clearProcedureStateOnExamChange(session,
                        transition.getExamId(), transition.getExamId());
            }
            if (transition.isPersistSelection()) {
                ExamStaffPageBinder.persistExamSelection(session, transition.getExamId(), transition.getExamId());
            }
        }

        // Service chuẩn bị context + bind UI
        ExamStaffPageCommand input = buildPagePrepareInput(request, session, webRoot, loadCandidates,
                urlExamId, v);
        ExamStaffPageContext ctx = v.preparePageContext(input);

        if (ExamStaffHttpSupport.parseExamIdParam(request) > 0 && ctx.getExamId() <= 0 && request != null) {
            request.setAttribute("examSelectError",
                    "Không tìm thấy kỳ thi (mã " + urlExamId + ").");
        }

        if (ctx.getExamId() > 0 && session != null) {
            ExamStaffPageBinder.persistExamSelection(session, ctx.getExamId(), ctx.getExamId());
        }

        if (ctx.getPickerView() != null) {
            ExamStaffPageBinder.bindPickerView(request, ctx.getPickerView());
        }

        CandidateQueueSnapshotDTO snapshot = v.buildQueueSnapshot(
                ctx.getCandidates(), ctx.getExamId(), ctx.getExamId());
        ExamStaffPageBinder.publishQueue(request, session, snapshot);

        if (ctx.getAllExams() == null) {
            ctx.setAllExams(List.of());
        }
        if (ctx.getCandidates() == null) {
            ctx.setCandidates(List.of());
        }
        return ctx;
    }

    /**
     * Liệt kê mọi kỳ thi (có inject view).
     * @param view ViewService; null → mặc định
     * @return danh sách kỳ
     */
    public static List<ExamSummaryDTO> loadAllExams(ExamStaffViewService view) {
        return (view != null ? view : VIEW).listAllExams();
    }

    /** Liệt kê mọi kỳ thi (view mặc định). */
    public static List<ExamSummaryDTO> loadAllExams() {
        return VIEW.listAllExams();
    }

    /**
     * Xóa cache queue thí sinh trên session.
     * @param session session staff
     */
    public static void clearCandidateCache(HttpSession session) {
        ExamStaffPageBinder.clearCandidateCache(session);
    }

    /**
     * Áp examId từ URL: resolve trong allExams rồi remember vào session.
     * Nếu không có examId URL → ủy quyền resolveExamId.
     * @return examId đã resolve; 0 nếu không hợp lệ
     */
    public static int applyExamIdFromRequest(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allExams, ExamStaffViewService view) {
        ExamStaffViewService v = view != null ? view : VIEW;
        int examId = ExamStaffHttpSupport.parseExamIdParam(request);
        if (examId <= 0) {
            return resolveExamId(request, session, allExams, 0, v);
        }
        int resolvedExamId = v.resolveExamFromUrl(examId, allExams);
        if (resolvedExamId <= 0) {
            return 0;
        }
        rememberExamId(session, examId, resolvedExamId);
        return resolvedExamId;
    }

    /** Overload applyExamIdFromRequest với view mặc định. */
    public static int applyExamIdFromRequest(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allExams) {
        return applyExamIdFromRequest(request, session, allExams, VIEW);
    }

    /**
     * Resolve examId từ URL/session/danh sách kỳ (ủy quyền ViewService).
     * @param defaultId examId mặc định khi không có nguồn nào
     * @return examId đã chọn
     */
    public static int resolveExamId(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allExams, int defaultId, ExamStaffViewService view) {
        return (view != null ? view : VIEW).resolveExamId(
                buildSelectionInput(request, session, allExams, defaultId));
    }

    /** Overload resolveExamId với view mặc định. */
    public static int resolveExamId(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allExams, int defaultId) {
        return resolveExamId(request, session, allExams, defaultId, VIEW);
    }

    /**
     * Ghi selectedExamId vào session (ưu tiên examId, fallback fallbackExamId).
     */
    public static void rememberExamId(HttpSession session, int fallbackExamId, int examId) {
        ExamStaffPageBinder.persistExamSelection(session, fallbackExamId, examId);
    }

    /**
     * Đảm bảo có examId hợp lệ: resolve + persist selected/primary.
     * @return examId > 0 hoặc 0
     */
    public static int ensureExamId(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allExams, ExamStaffViewService view) {
        ExamStaffViewService v = view != null ? view : VIEW;
        ExamStaffPageCommand input = buildSelectionInput(request, session, allExams, 0);
        int examId = v.ensureExamId(input);
        if (examId > 0 && session != null) {
            int primaryExamId = v.resolvePrimaryExamId(input.getAllExams(), examId);
            rememberExamId(session, primaryExamId, examId);
        }
        return examId;
    }

    /** Overload ensureExamId với view mặc định. */
    public static int ensureExamId(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allExams) {
        return ensureExamId(request, session, allExams, VIEW);
    }

    /**
     * Đồng bộ selectedExamId trên session sau thao tác (ví dụ thu phí).
     * So sánh với selected hiện tại qua ViewService rồi ghi lại.
     */
    public static void syncExamSelection(HttpSession session, List<ExamSummaryDTO> allExams, int examId,
            ExamStaffViewService view) {
        if (session == null || examId <= 0) {
            return;
        }
        ExamStaffViewService v = view != null ? view : VIEW;
        Integer currentExamId = ExamStaffPageBinder.readSelectedExamId(session);
        ExamTransitionResultDTO state = v.syncExamSelection(examId, currentExamId, allExams);
        session.setAttribute(ExamStaffSessionKeys.SELECTED_EXAM_ID,
                state.getExamId() > 0 ? state.getExamId() : examId);
    }

    /** Overload syncExamSelection với view mặc định. */
    public static void syncExamSelection(HttpSession session, List<ExamSummaryDTO> allExams, int examId) {
        syncExamSelection(session, allExams, examId, VIEW);
    }

    /**
     * Bind sidebar picker nếu request chưa có examOptions
     * (tránh ghi đè khi trang đã prepare đầy đủ).
     */
    public static void bindSidebarIfNeeded(HttpServletRequest request, HttpSession session,
            ExamStaffViewService view) {
        if (request == null || request.getAttribute("examOptions") != null) {
            return;
        }
        ExamStaffViewService v = view != null ? view : VIEW;
        List<ExamSummaryDTO> allExams = v.listAllExams();
        int examId = resolveExamId(request, session, allExams, 0, v);
        ExamStaffPageBinder.bindPickerView(request, v.buildPickerView(allExams, examId, 0));
        if (session != null) {
            @SuppressWarnings("unchecked")
            List<ExamSummaryDTO> options = (List<ExamSummaryDTO>) request.getAttribute("examOptions");
            if (options != null) {
                session.setAttribute("examStaffExamOptions", options);
            }
        }
    }

    /** Overload bindSidebarIfNeeded với view mặc định. */
    public static void bindSidebarIfNeeded(HttpServletRequest request, HttpSession session) {
        bindSidebarIfNeeded(request, session, VIEW);
    }

    /**
     * Tìm kỳ theo id trong danh sách (ủy quyền ViewService).
     * @return DTO hoặc null
     */
    public static ExamSummaryDTO findExamById(List<ExamSummaryDTO> allExams, int examId,
            ExamStaffViewService view) {
        return (view != null ? view : VIEW).findExamById(examId, allExams);
    }

    /** Overload findExamById với view mặc định. */
    public static ExamSummaryDTO findExamById(List<ExamSummaryDTO> allExams, int examId) {
        return findExamById(allExams, examId, VIEW);
    }

    /**
     * Kỳ đại diện cho examId (khi danh sách nhóm nhiều slot cùng ngày).
     * @return DTO đại diện hoặc null
     */
    public static ExamSummaryDTO representativeExam(List<ExamSummaryDTO> allExams, int examId,
            ExamStaffViewService view) {
        return (view != null ? view : VIEW).representativeExam(allExams, examId);
    }

    /** Overload representativeExam với view mặc định. */
    public static ExamSummaryDTO representativeExam(List<ExamSummaryDTO> allExams, int examId) {
        return representativeExam(allExams, examId, VIEW);
    }

    /**
     * Resolve mã kỳ “chính” (primary) từ examId có thể là slot phụ.
     * @return primary examId
     */
    public static int resolvePrimaryExamId(List<ExamSummaryDTO> allExams, int examId,
            ExamStaffViewService view) {
        return (view != null ? view : VIEW).resolvePrimaryExamId(allExams, examId);
    }

    /** Overload resolvePrimaryExamId với view mặc định. */
    public static int resolvePrimaryExamId(List<ExamSummaryDTO> allExams, int examId) {
        return resolvePrimaryExamId(allExams, examId, VIEW);
    }

    /**
     * Resolve kỳ từ param examId URL; nếu tìm thấy thì remember vào session.
     * @return ExamSummaryDTO hoặc null nếu không có/không khớp
     */
    public static ExamSummaryDTO resolveExamFromRequest(HttpServletRequest request, HttpSession httpSession,
            List<ExamSummaryDTO> allExams, ExamStaffViewService view) {
        int examId = ExamStaffHttpSupport.parseExamIdParam(request);
        if (examId <= 0) {
            return null;
        }
        ExamSummaryDTO picked = findExamById(allExams, examId, view);
        if (picked != null && picked.getExamId() > 0) {
            rememberExamId(httpSession, examId, picked.getExamId());
            return picked;
        }
        return null;
    }

    /** Overload resolveExamFromRequest với view mặc định. */
    public static ExamSummaryDTO resolveExamFromRequest(HttpServletRequest request, HttpSession httpSession,
            List<ExamSummaryDTO> allExams) {
        return resolveExamFromRequest(request, httpSession, allExams, VIEW);
    }

    /**
     * Build command chuẩn bị trang: URL/session/cache queue + call order.
     */
    private static ExamStaffPageCommand buildPagePrepareInput(HttpServletRequest request, HttpSession session,
            String webRoot, boolean loadCandidates, int urlExamId, ExamStaffViewService view) {
        ExamStaffPageCommand input = new ExamStaffPageCommand();
        input.setUrlExamId(urlExamId);
        input.setWebRoot(webRoot);
        input.setLoadCandidates(loadCandidates);
        input.setAllExams(view.listAllExams());
        if (request != null) {
            input.setExamIdParam(request.getParameter("examId"));
        }
        if (session != null) {
            input.setPreviousExamId(ExamStaffPageBinder.readSelectedExamId(session));
            input.setSelectedExamId(ExamStaffPageBinder.readSelectedExamId(session));
            input.setLoadedExamId(ExamStaffPageBinder.readLoadedExamId(session));
            @SuppressWarnings("unchecked")
            List<ExamRegistrationDTO> cached =
                    (List<ExamRegistrationDTO>) session.getAttribute(ExamStaffSessionKeys.CANDIDATE_QUEUE);
            input.setCachedQueue(cached);
            @SuppressWarnings("unchecked")
            List<String> order =
                    (List<String>) session.getAttribute(ExamStaffSessionKeys.CALL_QUEUE_ORDER);
            input.setCallQueueOrder(order);
            input.setCallQueueOrderExamId(ExamStaffPageBinder.readCallQueueOrderExamId(session));
        }
        return input;
    }

    /**
     * Build command cho bước transition đổi kỳ (chỉ URL + previous/loaded).
     */
    private static ExamStaffPageCommand buildPageTransitionInput(HttpSession session, int urlExamId,
            ExamStaffViewService view) {
        ExamStaffPageCommand input = new ExamStaffPageCommand();
        input.setUrlExamId(urlExamId);
        input.setAllExams(view.listAllExams());
        if (session != null) {
            input.setPreviousExamId(ExamStaffPageBinder.readSelectedExamId(session));
            input.setLoadedExamId(ExamStaffPageBinder.readLoadedExamId(session));
        }
        return input;
    }

    /**
     * Build command chọn kỳ: URL param + selected session + defaultId.
     */
    private static ExamStaffPageCommand buildSelectionInput(HttpServletRequest request,
            HttpSession session, List<ExamSummaryDTO> allExams, int defaultExamId) {
        ExamStaffPageCommand input = new ExamStaffPageCommand();
        input.setUrlExamId(ExamStaffHttpSupport.parseExamIdParam(request));
        input.setAllExams(allExams);
        input.setDefaultExamId(defaultExamId);
        if (request != null) {
            input.setExamIdParam(request.getParameter("examId"));
        }
        if (session != null) {
            input.setSelectedExamId(ExamStaffPageBinder.readSelectedExamId(session));
        }
        return input;
    }

    /** Set CharacterEncoding UTF-8 trên request (ignore lỗi container). */
    private static void applyUtf8Request(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (Exception ignored) {
        }
    }
}
