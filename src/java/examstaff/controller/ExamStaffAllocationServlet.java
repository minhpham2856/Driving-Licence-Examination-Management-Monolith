package examstaff.controller;

import examstaff.util.ExamStaffHttpSupport;
import examstaff.util.StaffAuditLogSupport;
import examstaff.dto.AllocationActionResultDTO;
import examstaff.dto.AllocationCandidateActionRequest;
import examstaff.dto.AllocationStageViewDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.service.impl.AllocationActionServiceImpl;
import examstaff.service.impl.AllocationStageViewServiceImpl;
import examstaff.service.impl.CandidateQueueServiceImpl;
import examstaff.service.impl.ExamAreaQueryServiceImpl;
import examstaff.service.impl.ExaminerAllocationDeskServiceImpl;
import examstaff.service.impl.ExaminerAllocationServiceImpl;
import examstaff.service.impl.StaffAuditLogServiceImpl;
import examstaff.util.AllocationStageHelper;
import examstaff.util.ExamRegistrationSort;
import examstaff.util.ExamStaffPageSupport;
import examstaff.util.ExamStaffPageSupport.PageContext;
import examstaff.util.SessionUserHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {
        "/views/staff/examstaff/allocation",
        "/views/staff/examstaff/allocation-waiting",
        "/views/staff/examstaff/allocation-theory",
        "/views/staff/examstaff/allocation-practical",
        "/views/staff/examstaff/allocation-results-pass",
        "/views/staff/examstaff/allocation-results-fail",
        "/views/staff/examstaff/allocation-results-suspended",
        "/views/staff/examstaff/examiner-allocation"
})
public class ExamStaffAllocationServlet extends HttpServlet {

    private final ExamAreaQueryServiceImpl areaQueryService = new ExamAreaQueryServiceImpl();
    private final AllocationStageViewServiceImpl allocationStageViewService = new AllocationStageViewServiceImpl();
    private final AllocationActionServiceImpl allocationActionService = new AllocationActionServiceImpl();
    private final CandidateQueueServiceImpl candidateQueueService = new CandidateQueueServiceImpl();
    private final ExaminerAllocationServiceImpl examinerAllocationService = new ExaminerAllocationServiceImpl();
    private final ExaminerAllocationDeskServiceImpl examinerAllocationDeskService =
            new ExaminerAllocationDeskServiceImpl();
    private final StaffAuditLogSupport auditLogSupport = new StaffAuditLogSupport(new StaffAuditLogServiceImpl());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        if (servletPath != null && servletPath.endsWith("examiner-allocation")) {
            handleExaminerAllocation(request, response);
            return;
        }

        HttpSession session = request.getSession();
        String stage = AllocationStageHelper.resolveStageFromServletPath(servletPath);
        String resultFilter = AllocationStageHelper.resolveResultFilterFromServletPath(servletPath);
        String jspPath = AllocationStageHelper.resolveJspPath(servletPath);

        try {
            request.removeAttribute("errorMsg");
            request.removeAttribute("warningMsg");
            request.removeAttribute("alertMsg");

            ExamStaffHttpSupport.applyNoCacheHeaders(response);
            String webRoot = request.getServletContext().getRealPath("/");

            int urlExamId = ExamStaffPageSupport.parseExamIdParam(request);
            if (Boolean.TRUE.equals(session.getAttribute("examStaffSessionJustChanged"))) {
                session.removeAttribute("examStaffSessionJustChanged");
                ExamStaffPageSupport.clearCandidateCache(session);
            }
            if (urlExamId > 0) {
                Integer loadedExam = ExamStaffPageSupport.readLoadedExamId(session);
                if (loadedExam == null || loadedExam != urlExamId) {
                    ExamStaffPageSupport.clearCandidateCache(session);
                }
                ExamStaffPageSupport.persistExamSelection(session, urlExamId, urlExamId);
            }

            PageContext pageCtx = ExamStaffPageSupport.preparePageContext(request, true);
            int examId = pageCtx.getExamId();
            List<ExamRegistrationDTO> qList = new ArrayList<>(pageCtx.getCandidates());
            publishCandidateQueue(request, session, qList, examId);
            session.setAttribute("lastLoadedExamId", examId);
            request.setAttribute("allocationActiveSessionId", examId);

            String action = request.getParameter("action");
            String regIdStr = request.getParameter("id");
            String searchQ = request.getParameter("q");
            if (searchQ == null) {
                searchQ = "";
            }
            int page = AllocationStageHelper.parsePage(request.getParameter("page"));
            int pageSize = AllocationStageHelper.parsePageSize(request.getParameter("size"));
            if (urlExamId > 0 && session != null) {
                Integer allocationPageExam = (Integer) session.getAttribute("allocationPageExamId");
                if (allocationPageExam != null && allocationPageExam > 0 && allocationPageExam != urlExamId) {
                    page = 1;
                }
                session.setAttribute("allocationPageExamId", urlExamId);
            }
            ExamRegistrationSort.Spec sortSpec = ExamRegistrationSort.parse(
                    request.getParameter("sort"), request.getParameter("dir"));
            request.setAttribute("sortBy", sortSpec.getColumn());
            request.setAttribute("sortDir", sortSpec.isAscending() ? "asc" : "desc");
            String examIdParam = examId > 0 ? String.valueOf(examId) : request.getParameter("examId");
            Integer areaFilterId = AllocationStageHelper.parseAreaFilter(request.getParameter("areaFilter"));

            if (action == null) {
                AllocationActionResultDTO overviewResult = allocationActionService.autoAllocateOnOverview(
                        examId, stage);
                if (overviewResult.getAllocatedCount() > 0) {
                    qList = refreshCandidateQueue(session, examId, webRoot, pageCtx.getAllSessions());
                    publishCandidateQueue(request, session, qList, examId);
                }
            }

            if (action != null) {
                try {
                    if ("autoAllocate".equals(action)) {
                        AllocationActionResultDTO allocResult = allocationActionService.autoAllocateOnOverview(
                                examId, AllocationStageHelper.STAGE_THEORY);
                        applyActionResult(request, session, allocResult);
                        if (allocResult.getAllocatedCount() > 0) {
                            qList = refreshCandidateQueue(session, examId, webRoot, pageCtx.getAllSessions());
                        }
                        stage = AllocationStageHelper.STAGE_THEORY;
                        servletPath = "/views/staff/examstaff/allocation-theory";
                        jspPath = AllocationStageHelper.resolveJspPath(servletPath);
                    } else if (regIdStr != null) {
                        int regId = Integer.parseInt(regIdStr);
                        ExamRegistrationDTO profile = allocationActionService.findCandidate(regId, examId, qList);
                        if (profile != null) {
                            AllocationCandidateActionRequest actionRequest = new AllocationCandidateActionRequest();
                            actionRequest.setAction(action);
                            actionRequest.setRegId(regId);
                            actionRequest.setExamId(examId);
                            actionRequest.setProfile(profile);
                            if ("allocateRoom".equals(action)) {
                                actionRequest.setAreaId(Integer.parseInt(request.getParameter("areaId")));
                            }
                            AllocationActionResultDTO actionResult = allocationActionService.executeCandidateAction(
                                    actionRequest);
                            applyActionResult(request, session, actionResult);
                            if (actionResult.getRedirectServletPath() != null) {
                                servletPath = actionResult.getRedirectServletPath();
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
                qList = refreshCandidateQueue(session, examId, webRoot, pageCtx.getAllSessions());
                publishCandidateQueue(request, session, qList, examId);
                session.setAttribute("lastLoadedExamId", examId);

                if (shouldRedirectAfterAction(action)) {
                    stashAllocationFlash(session, request);
                    response.sendRedirect(buildRedirectUrl(request, servletPath, examId, page, pageSize,
                            searchQ, sortSpec));
                    return;
                }
            }

            ExamStaffPageSupport.consumeFlash(session, "allocationFlashMsg", request, "alertMsg");
            ExamStaffPageSupport.consumeFlash(session, "allocationFlashError", request, "errorMsg");
            ExamStaffPageSupport.consumeFlash(session, "allocationFlashWarn", request, "warningMsg");

            publishStageData(request, qList, stage, resultFilter, searchQ, page, pageSize, sortSpec, areaFilterId);

            request.setAttribute("allocationListPath", servletPath);
            request.setAttribute("allocationExtraQuery", AllocationStageHelper.buildExtraQuery(
                    page, pageSize, searchQ, examIdParam,
                    sortSpec.getColumn(), sortSpec.isAscending() ? "asc" : "desc", areaFilterId));
            request.setAttribute("allocationSearchQuery", searchQ.trim());
            request.setAttribute("allocationPageSize", pageSize);
            try {
                request.setAttribute("activeTheoryRooms", areaQueryService.listActiveTheoryRooms());
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("activeTheoryRooms", List.of());
            }

            ExamStaffPageSupport.consumeFlash(session, "sessionSelectMsg", request, "sessionSelectMsg");
            ExamStaffPageSupport.consumeFlash(session, "sessionSelectError", request, "sessionSelectError");

            request.getRequestDispatcher(jspPath).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "Không tải được trang phân bổ: " + e.getMessage());
            publishStageData(request, List.of(), stage, resultFilter, "", 1,
                    AllocationStageHelper.DEFAULT_PAGE_SIZE,
                    ExamRegistrationSort.parse(null, null), null);
            request.setAttribute("allocationListPath", servletPath);
            request.setAttribute("allocationSearchQuery", "");
            request.setAttribute("allocationExtraQuery", "");
            request.setAttribute("allocationPageSize", AllocationStageHelper.DEFAULT_PAGE_SIZE);
            request.setAttribute("activeTheoryRooms", List.of());
            request.getRequestDispatcher(jspPath).forward(request, response);
        }
    }

    private void handleExaminerAllocation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        HttpSession session = request.getSession();

        request.removeAttribute("errorMsg");
        request.removeAttribute("alertMsg");

        ExamStaffPageSupport.consumeFlash(session, "sessionControlMsg", request, "alertMsg");
        ExamStaffPageSupport.consumeFlash(session, "sessionControlError", request, "errorMsg");
        ExamStaffPageSupport.consumeFlash(session, "sessionSelectMsg", request, "sessionSelectMsg");

        PageContext pageCtx = ExamStaffPageSupport.preparePageContext(request, false);
        List<ExamSummaryDTO> allSessions = pageCtx.getAllSessions();
        int examId = pageCtx.getExamId();

        int urlExamId = ExamStaffPageSupport.parseExamIdParam(request);
        if (urlExamId > 0) {
            ExamSummaryDTO picked = ExamStaffPageSupport.findExamById(allSessions, urlExamId);
            if (picked != null && picked.getExamId() > 0) {
                ExamStaffPageSupport.persistExamSelection(session, urlExamId, picked.getExamId());
                examId = urlExamId;
            }
        }

        ExamSummaryDTO currentExam = examId > 0 ? examinerAllocationService.getSessionById(examId) : null;
        if (currentExam == null && examId > 0) {
            currentExam = ExamStaffPageSupport.representativeSessionForExam(allSessions, examId);
        }

        request.setAttribute("allSessions", allSessions);
        request.setAttribute("currentExam", currentExam);
        request.setAttribute("selectedExamId", examId);

        String action = request.getParameter("action");
        if (action != null && examId > 0) {
            handleExaminerAction(request, session, action);
        }

        if (examId > 0) {
            var view = examinerAllocationDeskService.buildAllocationView(examId, examId, allSessions);
            request.setAttribute("examAssignments", view.getDayAssignments());
            request.setAttribute("allExaminers", view.getAllExaminers());
            request.setAttribute("availableExaminers", view.getAvailableExaminers());
            request.setAttribute("busyExaminers", view.getBusyExaminers());
            request.setAttribute("areaAssignOptions", view.getAreaAssignOptions());
            request.setAttribute("examStaffLoadedExamId", examId);
        }

        request.getRequestDispatcher("/views/staff/examstaff/examiner-allocation.jsp").forward(request, response);
    }

    private void handleExaminerAction(HttpServletRequest request, HttpSession session, String action) {
        try {
            if ("assign".equals(action)) {
                int targetExamId = Integer.parseInt(request.getParameter("targetExamId"));
                int areaId = Integer.parseInt(request.getParameter("areaId"));
                int examinerUserId = Integer.parseInt(request.getParameter("examinerUserId"));
                var result = examinerAllocationDeskService.assignExaminer(
                        targetExamId, areaId, examinerUserId, resolveStaffId(session));
                applyExaminerActionResult(request, session, result);
            } else if ("remove".equals(action)) {
                var result = examinerAllocationDeskService.removeExaminer(request.getParameter("slotKey"));
                applyExaminerActionResult(request, session, result);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMsg", "Dữ liệu không hợp lệ.");
        }
    }

    private void applyExaminerActionResult(HttpServletRequest request, HttpSession session,
            examstaff.dto.ExaminerAllocationActionResultDTO result) {
        if (result.getAlertMsg() != null) {
            request.setAttribute("alertMsg", result.getAlertMsg());
        }
        if (result.getErrorMsg() != null) {
            request.setAttribute("errorMsg", result.getErrorMsg());
        }
        if (result.isSuccess() && result.getAuditAction() != null) {
            auditLogSupport.persist(session, result.getAuditAction(), result.getAuditDetails());
        }
    }

    private int resolveStaffId(HttpSession session) {
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user != null && user.getUserId() > 0) {
            return user.getUserId();
        }
        return SessionUserHelper.resolveUserId(session);
    }

    private void publishStageData(HttpServletRequest request, List<ExamRegistrationDTO> qList,
            String stage, String resultFilter, String searchQ, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec, Integer areaFilterId) {
        AllocationStageViewDTO view = allocationStageViewService.buildView(
                qList, stage, resultFilter, searchQ, page, pageSize, sortSpec, areaFilterId);
        request.setAttribute("allocationStageCounts", view.getStageCounts());
        request.setAttribute("allocationStageList", view.getStageList());
        request.setAttribute("allocationPageSlice", view.getPageSlice());
        request.setAttribute("allocationOverviewHits", view.getOverviewSearchHits());
    }

    private List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, String webRoot,
            List<ExamSummaryDTO> allSessions) {
        if (session == null) {
            return List.of();
        }
        ExamStaffQueueRefreshInput input = new ExamStaffQueueRefreshInput();
        input.setExamId(examId);
        input.setWebRoot(webRoot);
        input.setAllSessions(allSessions);
        input.setSelectedExamId(ExamStaffPageSupport.readSelectedExamId(session));
        @SuppressWarnings("unchecked")
        List<String> order = (List<String>) session.getAttribute("callQueueOrder");
        input.setCallQueueOrder(order);
        input.setCallQueueOrderExamId(ExamStaffPageSupport.readCallQueueOrderExamId(session));

        CandidateQueueSnapshotDTO snapshot = candidateQueueService.refreshQueue(input);
        ExamStaffPageSupport.publishQueue(null, session, snapshot);
        return snapshot.getFullQueue();
    }

    private void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, int examId) {
        CandidateQueueSnapshotDTO snapshot = candidateQueueService.buildSnapshot(qList, examId, examId);
        ExamSummaryDTO current = ExamStaffPageSupport.findExamById(ExamStaffPageSupport.loadAllExams(), examId);
        if (current == null && examId > 0) {
            current = ExamStaffPageSupport.representativeSessionForExam(
                    ExamStaffPageSupport.loadAllExams(), examId);
        }
        ExamStaffPageSupport.publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), examId, examId, current);
    }

    private void applyActionResult(HttpServletRequest request, HttpSession session,
            AllocationActionResultDTO result) {
        if (result == null) {
            return;
        }
        if (result.getErrorMsg() != null) {
            request.setAttribute("errorMsg", result.getErrorMsg());
        }
        if (result.getAlertMsg() != null) {
            request.setAttribute("alertMsg", result.getAlertMsg());
        }
        if (result.hasAuditLog()) {
            auditLogSupport.persist(session, result.getAuditAction(), result.getAuditDetails(),
                    result.getAuditRecordId());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private static boolean shouldRedirectAfterAction(String action) {
        return action != null && !"callCandidate".equals(action);
    }

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
        Object warn = request.getAttribute("warningMsg");
        if (warn != null) {
            session.setAttribute("allocationFlashWarn", warn);
        }
    }

    private static String buildRedirectUrl(HttpServletRequest request, String servletPath, int examId,
            int page, int pageSize, String searchQ, ExamRegistrationSort.Spec sortSpec) {
        String extra = AllocationStageHelper.buildExtraQuery(page, pageSize, searchQ,
                examId > 0 ? String.valueOf(examId) : null,
                sortSpec.getColumn(), sortSpec.isAscending() ? "asc" : "desc", null);
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
