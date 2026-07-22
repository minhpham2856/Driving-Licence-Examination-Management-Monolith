package examstaff.controller;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.AllocationActionResultDTO;
import examstaff.dto.AllocationCandidateActionRequest;
import examstaff.dto.AllocationStageViewDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffPageCommand;
import examstaff.dto.ExamStaffPageContext;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ServiceResult;
import examstaff.service.AllocationService;
import examstaff.service.AuditService;
import examstaff.service.ExamStaffViewService;
import examstaff.service.impl.AllocationServiceImpl;
import examstaff.service.impl.AuditServiceImpl;
import examstaff.service.impl.ExamStaffViewServiceImpl;
import examstaff.util.ExamRegistrationSort;
import examstaff.service.impl.support.allocation.AllocationStageHelper;
import shared.Attributes;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Các trang phân bổ thí sinh theo giai đoạn (waiting/theory/practical/results):
 * điều phối HTTP ↔ AllocationAction/StageView ↔ redirect/forward JSP theo servlet path.
 *
 * Cách URL map sang stage:
 *
 * {@link AllocationStageHelper#resolveStageFromServletPath} đọc {@code request.getServletPath()}:
 * ví dụ {@code /examstaff/allocation-theory} → stage {@code theory} + JSP tương ứng.
 * Một servlet, nhiều {@code @WebServlet} patterns — tránh nhân đôi code controller.
 *
 * Luồng GET (và POST ủy quyền GET):
 * - Chuẩn bị kỳ thi + queue thí sinh ({@code ExamStaffPageSupport})
 * - Nếu có {@code action} (allocateRoom / allocatePracticalRoom):
 *       {@code AllocationService.executeCandidateAction} → flash → redirect PRG
 * - Overview không action: có thể {@code autoAllocateOnOverview}
 * - Không action: bind danh sách stage → forward JSP
 * <p>Ghi DB phân phòng nằm ở DAO ({@code ExamEnrollmentSectionSupport}); servlet chỉ HTTP.
 */
@WebServlet(urlPatterns = {
        "/examstaff/allocation",
        "/examstaff/allocation-waiting",
        "/examstaff/allocation-theory",
        "/examstaff/allocation-practical",
        "/examstaff/allocation-results-pass",
        "/examstaff/allocation-results-fail",
        "/examstaff/allocation-results-suspended"
})
public class AllocationServlet extends HttpServlet {

    private final AllocationService allocationService = new AllocationServiceImpl();
    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();
    private final AuditService auditService = new AuditServiceImpl();

    /**
     * GET: resolve stage từ path → prepare page → (action → PRG redirect) hoặc bind danh sách → forward JSP.
     * <p>
 *
     * Luồng chính:
     * - Resolve stage/resultFilter/jsp từ servlet path
     * - Chuẩn bị kỳ + queue; xử lý đổi kỳ / auto-allocate overview
     * - Nếu có action: execute → flash → redirect PRG
     * - Không action: consume flash → publish stage → forward JSP
     * @throws ServletException lỗi forward
     * @throws IOException      lỗi redirect / 500
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String servletPath = request.getServletPath();
        // Map URL → stage (waiting/theory/…) + JSP
        String stage = AllocationStageHelper.resolveStageFromServletPath(servletPath);
        String resultFilter = AllocationStageHelper.resolveResultFilterFromServletPath(servletPath);
        String jspPath = AllocationStageHelper.resolveJspPath(servletPath);

        try {
            request.removeAttribute("errorMsg");
            request.removeAttribute("alertMsg");

            ExamStaffHttpSupport.applyNoCacheHeaders(response);
            String webRoot = request.getServletContext().getRealPath("/");

            // Đổi kỳ → clear cache queue cũ
            int urlExamId = ExamStaffHttpSupport.parseExamIdParam(request);
            if (Boolean.TRUE.equals(session.getAttribute("examStaffExamJustChanged"))) {
                session.removeAttribute("examStaffExamJustChanged");
                ExamStaffPageSupport.clearCandidateCache(session);
            }
            if (urlExamId > 0) {
                Integer loadedExam = (Integer) session.getAttribute(ExamStaffSessionKeys.LOADED_EXAM_ID);
                if (loadedExam == null || loadedExam != urlExamId) {
                    ExamStaffPageSupport.clearCandidateCache(session);
                }
                ExamStaffPageSupport.applyExamIdFromRequest(request, session,
                        viewService.listAllExams(), viewService);
            }

            ExamStaffPageContext pageCtx = ExamStaffPageSupport.prepareExamStaffPage(
                    request, session, webRoot, true, viewService);
            int examId = pageCtx.getExamId();
            List<ExamRegistrationDTO> qList = new ArrayList<>(pageCtx.getCandidates());
            publishCandidateQueue(request, session, qList, examId);
            session.setAttribute(ExamStaffSessionKeys.LAST_LOADED_EXAM_ID, examId);
            request.setAttribute("allocationActiveExamId", examId);

            // Tham số lọc/paging/sort + action thí sinh
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
                // Đổi kỳ trên URL → về trang 1
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

            // Overview: tự phân bổ nếu chưa có action tường minh
            if (action == null) {
                ServiceResult<AllocationActionResultDTO> overviewResult =
                        allocationService.autoAllocateOnOverview(examId, stage);
                AllocationActionResultDTO overviewData = overviewResult.getData();
                if (overviewData != null && overviewData.getAllocatedCount() > 0) {
                    qList = refreshCandidateQueue(session, examId, webRoot, pageCtx.getAllExams());
                    publishCandidateQueue(request, session, qList, examId);
                }
            }

            // Nhánh thao tác thí sinh → PRG
            if (action != null) {
                try {
                    if (regIdStr != null) {
                        int regId = Integer.parseInt(regIdStr);
                        ExamRegistrationDTO profile = allocationService.findCandidate(regId, examId, qList);
                        if (profile != null) {
                            AllocationCandidateActionRequest actionRequest = new AllocationCandidateActionRequest();
                            actionRequest.setAction(action);
                            actionRequest.setRegId(regId);
                            actionRequest.setExamId(examId);
                            actionRequest.setProfile(profile);
                            if ("allocateRoom".equals(action) || "allocatePracticalRoom".equals(action)) {
                                actionRequest.setAreaId(Integer.parseInt(request.getParameter("areaId")));
                            }
                            ServiceResult<AllocationActionResultDTO> actionResult =
                                    allocationService.executeCandidateAction(actionRequest);
                            AllocationActionResultDTO data = actionResult.getData();
                            applyActionResult(request, session, actionResult);
                            if (data != null && data.getRedirectServletPath() != null) {
                                servletPath = data.getRedirectServletPath();
                                stage = AllocationStageHelper.resolveStageFromServletPath(servletPath);
                                resultFilter = AllocationStageHelper.resolveResultFilterFromServletPath(servletPath);
                                jspPath = AllocationStageHelper.resolveJspPath(servletPath);
                            }
                        } else {
                            request.setAttribute("errorMsg", "Không tìm thấy thí sinh để xử lý.");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setAttribute("errorMsg", "Lỗi xử lý: " + e.getMessage());
                }

                ExamStaffPageSupport.clearCandidateCache(session);
                qList = refreshCandidateQueue(session, examId, webRoot, pageCtx.getAllExams());
                publishCandidateQueue(request, session, qList, examId);
                session.setAttribute(ExamStaffSessionKeys.LAST_LOADED_EXAM_ID, examId);

                stashAllocationFlash(session, request);
                response.sendRedirect(buildRedirectUrl(request, servletPath, examId, page, pageSize,
                        searchQ, sortSpec, areaFilterId));
                return;
            }

            // Hiển thị danh sách stage
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
                        allocationService.listStaffedTheoryRoomsForExam(examId));
                request.setAttribute(Attributes.ExamStaff.ACTIVE_PRACTICAL_AREAS,
                        allocationService.listStaffedPracticalAreasForExam(examId));
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

    /**
     * Bind dữ liệu stage (counts/list/paging) lên request.
     * @param qList        queue đầy đủ
     * @param stage        waiting/theory/practical/results
     * @param resultFilter lọc kết quả (pass/fail/…) nếu có
     */
    private void publishStageData(HttpServletRequest request, List<ExamRegistrationDTO> qList,
            String stage, String resultFilter, String searchQ, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec, Integer areaFilterId) {
        AllocationStageViewDTO view = viewService.buildAllocationStageView(
                qList, stage, resultFilter, searchQ, page, pageSize, sortSpec, areaFilterId);
        if (view != null) {
            request.setAttribute("allocationStageCounts", view.getStageCounts());
            request.setAttribute("allocationStageList", view.getStageList());
            request.setAttribute("allocationPageSlice", view.getPageSlice());
            request.setAttribute("allocationOverviewHits", view.getOverviewSearchHits());
        }
    }

    /**
     * Refresh queue từ DB/service rồi publish snapshot vào session.
     * @return full queue sau refresh (không null)
     */
    private List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId,
            String webRoot, List<ExamSummaryDTO> allExams) {
        if (session == null) {
            return List.of();
        }
        ExamStaffPageCommand input = new ExamStaffPageCommand();
        input.setExamId(examId);
        input.setWebRoot(webRoot);
        input.setAllExams(allExams);
        input.setSelectedExamId(ExamStaffPageBinder.readSelectedExamId(session));
        @SuppressWarnings("unchecked")
        List<String> order = (List<String>) session.getAttribute(ExamStaffSessionKeys.CALL_QUEUE_ORDER);
        input.setCallQueueOrder(order);
        input.setCallQueueOrderExamId(ExamStaffPageBinder.readCallQueueOrderExamId(session));

        CandidateQueueSnapshotDTO snapshot = viewService.refreshQueue(input);
        ExamStaffPageBinder.publishQueue(null, session, snapshot);
        return snapshot.getFullQueue() != null ? snapshot.getFullQueue() : List.of();
    }

    /**
     * Publish full/active/procedure-done queue lên request + session cho JSP.
     * @param qList  queue đầy đủ
     * @param examId kỳ hiện tại
     */
    private void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, int examId) {
        CandidateQueueSnapshotDTO snapshot = viewService.buildQueueSnapshot(qList, examId, examId);
        ExamSummaryDTO current = viewService.findExamById(examId, viewService.listAllExams());
        if (current == null && examId > 0) {
            current = viewService.representativeExam(viewService.listAllExams(), examId);
        }
        ExamStaffPageBinder.publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), examId, examId, current);
    }

    /**
     * Set alert/error request và ghi audit nếu actionResult có log.
     * @param result kết quả thao tác phân bổ
     */
    private void applyActionResult(HttpServletRequest request, HttpSession session,
            ServiceResult<AllocationActionResultDTO> result) {
        if (request == null || result == null) {
            return;
        }
        AllocationActionResultDTO data = result.getData();
        if (!result.isSuccess()) {
            if (result.getMessage() != null) {
                request.setAttribute("errorMsg", result.getMessage());
            } else if (data != null && data.getErrorMsg() != null) {
                request.setAttribute("errorMsg", data.getErrorMsg());
            }
        } else if (result.getMessage() != null) {
            request.setAttribute("alertMsg", result.getMessage());
        } else if (data != null && data.getAlertMsg() != null) {
            request.setAttribute("alertMsg", data.getAlertMsg());
        }
        if (session != null && data != null && data.hasAuditLog()) {
            auditService.logAction(SessionUserHelper.resolveUserId(session),
                    data.getAuditAction(), data.getAuditDetails(), data.getAuditRecordId());
        }
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
     * @return URL tuyệt đối trong context
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
