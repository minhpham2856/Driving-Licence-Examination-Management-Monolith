package examstaff.controller.staff.exam;

import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.adapter.StaffAuditLogSupport;
import examstaff.controller.staff.exam.binder.AllocationActionResultBinder;
import examstaff.controller.staff.exam.binder.AllocationStageViewBinder;
import examstaff.controller.staff.exam.http.CandidateQueueHttpSupport;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.http.ExamStaffSessionKeys;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.controller.staff.exam.page.ExamStaffPageFacade;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.AllocationActionResultDTO;
import examstaff.dto.AllocationCandidateActionRequest;
import examstaff.dto.ExamSummaryDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import examstaff.service.AllocationActionService;
import examstaff.service.AllocationStageViewService;
import examstaff.service.CandidateQueueService;
import examstaff.service.ExamAreaQueryService;
import examstaff.service.ExamStaffServices;
import examstaff.util.ExamRegistrationSort;
import examstaff.util.AllocationStageHelper;
import shared.Attributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Các trang phân bổ thí sinh theo giai đoạn (waiting/theory/practical/results):
 * điều phối HTTP ↔ AllocationAction/StageView ↔ redirect/forward JSP theo servlet path.
 */
@WebServlet(urlPatterns = {
        "/views/staff/examstaff/allocation",
        "/views/staff/examstaff/allocation-waiting",
        "/views/staff/examstaff/allocation-theory",
        "/views/staff/examstaff/allocation-practical",
        "/views/staff/examstaff/allocation-results-pass",
        "/views/staff/examstaff/allocation-results-fail",
        "/views/staff/examstaff/allocation-results-suspended"
})
public class AllocationServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = ExamStaffWebModule.getInstance();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final ExamAreaQueryService areaQueryService;
    private final AllocationStageViewService allocationStageViewService;
    private final AllocationActionService allocationActionService;
    private final CandidateQueueService candidateQueueService;
    private final StaffAuditLogSupport auditLogSupport = MODULE.auditLogSupport();
    private final ExamStaffSelectionFacade selectionFacade = MODULE.selectionFacade();

    /** Constructor mặc định — lấy services từ composition root. */
    public AllocationServlet() {
        this(SERVICES.examAreas(),
                SERVICES.allocationStageView(), SERVICES.allocationActions(), SERVICES.candidateQueue());
    }

    /**
     * Constructor inject (test / wiring tay).
     *
     * @param areaQueryService           truy vấn phòng/khu vực
     * @param allocationStageViewService build view theo stage
     * @param allocationActionService    action phân bổ thí sinh
     * @param candidateQueueService      refresh/publish queue
     */
    AllocationServlet(ExamAreaQueryService areaQueryService,
            AllocationStageViewService allocationStageViewService,
            AllocationActionService allocationActionService,
            CandidateQueueService candidateQueueService) {
        this.areaQueryService = areaQueryService;
        this.allocationStageViewService = allocationStageViewService;
        this.allocationActionService = allocationActionService;
        this.candidateQueueService = candidateQueueService;
    }

    /**
     * GET: resolve stage từ path → prepare page → xử lý action (redirect) hoặc bind danh sách → forward JSP.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String servletPath = request.getServletPath();
        String stage = AllocationStageHelper.resolveStageFromServletPath(servletPath);
        String resultFilter = AllocationStageHelper.resolveResultFilterFromServletPath(servletPath);
        String jspPath = AllocationStageHelper.resolveJspPath(servletPath);

        try {
            request.removeAttribute("errorMsg");
            request.removeAttribute("alertMsg");

            ExamStaffHttpSupport.applyNoCacheHeaders(response);
            String webRoot = request.getServletContext().getRealPath("/");

            int urlExamId = ExamStaffHttpSupport.parseExamIdParam(request);
            if (Boolean.TRUE.equals(session.getAttribute("examStaffExamJustChanged"))) {
                session.removeAttribute("examStaffExamJustChanged");
                selectionFacade.clearCandidateCache(session);
            }
            if (urlExamId > 0) {
                Integer loadedExam = (Integer) session.getAttribute("examStaffLoadedExamId");
                if (loadedExam == null) {
                    loadedExam = (Integer) session.getAttribute("examStaffLoadedExamId");
                }
                if (loadedExam == null || loadedExam != urlExamId) {
                    selectionFacade.clearCandidateCache(session);
                } else {
                    ExamSummaryDTO urlExam = selectionFacade.findExamById(
                            selectionFacade.loadAllExams(), urlExamId);
                    if (urlExam != null && urlExam.getExamId() > 0
                            && urlExam.getExamId() != loadedExam) {
                        selectionFacade.clearCandidateCache(session);
                    }
                }
                selectionFacade.applyExamIdFromRequest(request, session,
                        selectionFacade.loadAllExams());
            }

            ExamStaffPageFacade.ExamStaffPageContext pageCtx = ExamStaffPageFacade.prepareExamStaffPage(
                    request, session, webRoot);
            int examId = pageCtx.getExamId();
            List<ExamRegistrationDTO> qList = new ArrayList<>(pageCtx.getCandidates());
            publishCandidateQueue(request, session, qList, examId);
            session.setAttribute(ExamStaffSessionKeys.LAST_LOADED_EXAM_ID, examId);
            request.setAttribute("allocationActiveExamId", examId);

            String action = request.getParameter("action");
            String regIdStr = request.getParameter("id");
            String searchQ = request.getParameter("q");
            if (searchQ == null) {
                searchQ = "";
            }
            Integer areaFilterId = AllocationStageHelper.parseAreaFilter(request.getParameter("areaFilter"));
            int page = AllocationStageHelper.parsePage(request.getParameter("page"));
            int pageSize = AllocationStageHelper.parsePageSize(request.getParameter("size"));
            if (urlExamId > 0 && session != null) {
                Integer allocationPageExam = (Integer) session.getAttribute("allocationPageExamId");
                if (allocationPageExam == null) {
                    allocationPageExam = (Integer) session.getAttribute("allocationPageExamId");
                }
                if (allocationPageExam != null && allocationPageExam > 0
                        && allocationPageExam != urlExamId) {
                    page = 1;
                }
                session.setAttribute("allocationPageExamId", urlExamId);
            }
            ExamRegistrationSort.Spec sortSpec = ExamRegistrationSort.parse(
                    request.getParameter("sort"), request.getParameter("dir"));
            request.setAttribute("sortBy", sortSpec.getColumn());
            request.setAttribute("sortDir", sortSpec.isAscending() ? "asc" : "desc");
            String examIdParam = examId > 0 ? String.valueOf(examId) : request.getParameter("examId");

            if (action == null) {
                AllocationActionResultDTO overviewResult = allocationActionService.autoAllocateOnOverview(
                        examId, stage);
                if (overviewResult.getAllocatedCount() > 0) {
                    qList = refreshCandidateQueue(session, examId, webRoot, pageCtx.getAllExams());
                    publishCandidateQueue(request, session, qList, examId);
                }
            }

            if (action != null) {
                try {
                    if (regIdStr != null) {
                        int regId = Integer.parseInt(regIdStr);
                        ExamRegistrationDTO profile = allocationActionService.findCandidate(regId, examId, qList);
                        if (profile != null) {
                            AllocationCandidateActionRequest actionRequest = new AllocationCandidateActionRequest();
                            actionRequest.setAction(action);
                            actionRequest.setRegId(regId);
                            actionRequest.setExamId(examId);
                            actionRequest.setProfile(profile);
                            if ("allocateRoom".equals(action) || "allocatePracticalRoom".equals(action)) {
                                actionRequest.setAreaId(Integer.parseInt(request.getParameter("areaId")));
                            }
                            AllocationActionResultDTO actionResult = allocationActionService.executeCandidateAction(
                                    actionRequest);
                            AllocationActionResultBinder.apply(request, session, actionResult, auditLogSupport);
                            servletPath = actionResult.getRedirectServletPath();
                            stage = AllocationStageHelper.resolveStageFromServletPath(servletPath);
                            resultFilter = AllocationStageHelper.resolveResultFilterFromServletPath(servletPath);
                            jspPath = AllocationStageHelper.resolveJspPath(servletPath);
                        } else {
                            request.setAttribute("errorMsg", "Không tìm thấy thí sinh để xử lý.");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setAttribute("errorMsg", "Lỗi xử lý: " + e.getMessage());
                }

                selectionFacade.clearCandidateCache(session);
                qList = refreshCandidateQueue(session, examId, webRoot, pageCtx.getAllExams());
                publishCandidateQueue(request, session, qList, examId);
                session.setAttribute(ExamStaffSessionKeys.LAST_LOADED_EXAM_ID, examId);

                stashAllocationFlash(session, request);
                response.sendRedirect(buildRedirectUrl(request, servletPath, examId, page, pageSize,
                        searchQ, sortSpec, areaFilterId));
                return;
            }

            ExamStaffHttpSupport.consumeFlash(session, "allocationFlashMsg", request, "alertMsg");
            ExamStaffHttpSupport.consumeFlash(session, "allocationFlashError", request, "errorMsg");

            publishStageData(request, qList, stage, resultFilter, searchQ, page, pageSize, sortSpec, areaFilterId);

            request.setAttribute("allocationListPath", servletPath);
            request.setAttribute("allocationExtraQuery", AllocationStageHelper.buildExtraQuery(
                    page, pageSize, searchQ, examIdParam,
                    sortSpec.getColumn(), sortSpec.isAscending() ? "asc" : "desc", areaFilterId));
            request.setAttribute("allocationSearchQuery", searchQ.trim());
            request.setAttribute(Attributes.ExamStaff.ALLOCATION_AREA_FILTER, areaFilterId);
            try {
                request.setAttribute(Attributes.ExamStaff.ACTIVE_THEORY_ROOMS,
                        areaQueryService.listStaffedTheoryRoomsForExam(examId));
                request.setAttribute(Attributes.ExamStaff.ACTIVE_PRACTICAL_AREAS,
                        areaQueryService.listStaffedPracticalAreasForExam(examId));
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute(Attributes.ExamStaff.ACTIVE_THEORY_ROOMS, List.of());
                request.setAttribute(Attributes.ExamStaff.ACTIVE_PRACTICAL_AREAS, List.of());
            }

            ExamStaffHttpSupport.consumeFlash(session, "examSelectMsg", request, "examSelectMsg");
            ExamStaffHttpSupport.consumeFlash(session, "examSelectError", request, "examSelectError");

            request.getRequestDispatcher(jspPath).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "Không tải được trang phân bổ: " + e.getMessage());
            publishStageData(request, List.of(), stage, resultFilter, "", 1,
                    AllocationStageHelper.DEFAULT_PAGE_SIZE,
                    ExamRegistrationSort.parse(null, null), null);
            request.setAttribute("allocationListPath", servletPath);
            request.setAttribute("allocationSearchQuery", "");
            request.setAttribute(Attributes.ExamStaff.ALLOCATION_AREA_FILTER, null);
            request.setAttribute("allocationExtraQuery", "");
            request.setAttribute(Attributes.ExamStaff.ACTIVE_THEORY_ROOMS, List.of());
            request.setAttribute(Attributes.ExamStaff.ACTIVE_PRACTICAL_AREAS, List.of());
            try {
                request.getRequestDispatcher(jspPath).forward(request, response);
            } catch (Exception forwardError) {
                forwardError.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Không tải được trang phân bổ: " + e.getMessage());
            }
        }
    }

    /** Bind dữ liệu stage (counts/list/paging) lên request qua {@link AllocationStageViewBinder}. */
    private void publishStageData(HttpServletRequest request, List<ExamRegistrationDTO> qList,
            String stage, String resultFilter, String searchQ, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec, Integer areaFilterId) {
        AllocationStageViewBinder.bind(request,
                allocationStageViewService.buildView(
                        qList, stage, resultFilter, searchQ, page, pageSize, sortSpec, areaFilterId));
    }

    /** Refresh queue từ DB/service rồi publish snapshot vào session. */
    private List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId,
            String webRoot, List<ExamSummaryDTO> allExams) {
        return CandidateQueueHttpSupport.refreshAndPublish(null, session, candidateQueueService,
                examId, examId, webRoot, allExams);
    }

    /** Publish full/active/procedure-done queue lên request + session cho JSP. */
    private void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, int examId) {
        CandidateQueueHttpSupport.publishLists(request, session, candidateQueueService,
                selectionFacade, qList, examId);
    }

    /** POST dùng chung luồng GET (action thường gửi form POST). */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /** Đưa alert/error request sang flash session trước redirect PRG. */
    private static void stashAllocationFlash(HttpSession session, HttpServletRequest request) {
        if (session == null || request == null) {
            return;
        }
        Object alert = request.getAttribute("alertMsg");
        if (alert != null) {
            session.setAttribute("allocationFlashMsg", alert);
        }
        Object error = request.getAttribute("errorMsg");
        if (error != null) {
            session.setAttribute("allocationFlashError", error);
        }
    }

    /**
     * Xây URL redirect sau action (giữ paging/search/sort/area + cache-buster).
     */
    private static String buildRedirectUrl(HttpServletRequest request, String servletPath, int examId,
            int page, int pageSize, String searchQ, ExamRegistrationSort.Spec sortSpec, Integer areaFilterId) {
        String extra = AllocationStageHelper.buildExtraQuery(page, pageSize, searchQ,
                examId > 0 ? String.valueOf(examId) : null,
                sortSpec.getColumn(), sortSpec.isAscending() ? "asc" : "desc", areaFilterId);
        StringBuilder url = new StringBuilder(request.getContextPath()).append(servletPath);
        if (extra != null && !extra.isBlank()) {
            url.append('?').append(extra.startsWith("&") ? extra.substring(1) : extra);
        } else if (examId > 0) {
            url.append("?examId=").append(examId);
        } else {
            url.append("?_=").append(System.currentTimeMillis());
            return url.toString();
        }
        url.append("&_=").append(System.currentTimeMillis());
        return url.toString();
    }
}
