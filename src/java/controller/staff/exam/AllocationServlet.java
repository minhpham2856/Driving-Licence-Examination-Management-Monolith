package controller.staff.exam;

import controller.staff.exam.adapter.CallBoardHttpFacade;
import controller.staff.exam.adapter.ExamStaffSelectionFacade;
import controller.staff.exam.adapter.StaffAuditLogSupport;
import controller.staff.exam.binder.AllocationActionResultBinder;
import controller.staff.exam.binder.AllocationStageViewBinder;
import controller.staff.exam.binder.ExamStaffPageBinder;
import controller.staff.exam.http.ExamStaffHttpSupport;
import controller.staff.exam.module.ExamStaffWebModule;
import controller.staff.exam.page.ExamStaffPageFacade;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.AllocationActionResultDTO;
import dto.examstaff.AllocationCandidateActionRequest;
import dto.examstaff.CandidateQueueSnapshotDTO;
import dto.examstaff.CandidateCallBoardStateDTO;
import dto.examstaff.ExamStaffQueueRefreshInput;
import dto.SessionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.AllocationActionService;
import service.AllocationStageViewService;
import service.CandidateQueueService;
import service.ExamAreaQueryService;
import service.ExamStaffServices;
import util.ExamRegistrationSort;
import util.examstaff.AllocationStageHelper;

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
        "/views/staff/examstaff/allocation-results-suspended"
})
public class AllocationServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = new ExamStaffWebModule();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final ExamAreaQueryService areaQueryService;
    private final AllocationStageViewService allocationStageViewService;
    private final AllocationActionService allocationActionService;
    private final CandidateQueueService candidateQueueService;
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();
    private final StaffAuditLogSupport auditLogSupport = MODULE.auditLogSupport();
    private final ExamStaffSelectionFacade selectionFacade = MODULE.selectionFacade();

    public AllocationServlet() {
        this(SERVICES.examAreas(),
                SERVICES.allocationStageView(), SERVICES.allocationActions(), SERVICES.candidateQueue());
    }

    AllocationServlet(ExamAreaQueryService areaQueryService,
            AllocationStageViewService allocationStageViewService,
            AllocationActionService allocationActionService,
            CandidateQueueService candidateQueueService) {
        this.areaQueryService = areaQueryService;
        this.allocationStageViewService = allocationStageViewService;
        this.allocationActionService = allocationActionService;
        this.candidateQueueService = candidateQueueService;
    }

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
            request.removeAttribute("warningMsg");
            request.removeAttribute("alertMsg");

            ExamStaffHttpSupport.applyNoCacheHeaders(response);
            String webRoot = request.getServletContext().getRealPath("/");

            int urlSessionId = ExamStaffHttpSupport.parseSessionIdParam(request);
            if (Boolean.TRUE.equals(session.getAttribute("examStaffSessionJustChanged"))) {
                session.removeAttribute("examStaffSessionJustChanged");
                selectionFacade.clearCandidateCache(session);
            }
            if (urlSessionId > 0) {
                Integer loadedSession = (Integer) session.getAttribute("examStaffLoadedSessionId");
                Integer loadedExam = (Integer) session.getAttribute("examStaffLoadedExamId");
                if (loadedSession == null || loadedSession != urlSessionId) {
                    selectionFacade.clearCandidateCache(session);
                } else if (loadedExam != null && loadedExam > 0) {
                    SessionDTO urlSession = selectionFacade.findSessionById(
                            selectionFacade.loadAllSessions(), urlSessionId);
                    if (urlSession != null && urlSession.getExamId() > 0
                            && urlSession.getExamId() != loadedExam) {
                        selectionFacade.clearCandidateCache(session);
                    }
                }
                selectionFacade.applySessionIdFromRequest(request, session,
                        selectionFacade.loadAllSessions());
            }

            ExamStaffPageFacade.ExamStaffPageContext pageCtx = ExamStaffPageFacade.prepareExamStaffPage(
                    request, session, webRoot);
            int examId = pageCtx.getExamId();
            int sessionId = pageCtx.getSessionId();
            List<ExamRegistrationDTO> qList = new ArrayList<>(pageCtx.getCandidates());
            publishCandidateQueue(request, session, qList, examId, sessionId);
            session.setAttribute("lastLoadedSessionId", sessionId);
            request.setAttribute("allocationActiveSessionId", sessionId);

            String action = request.getParameter("action");
            String regIdStr = request.getParameter("id");
            String searchQ = request.getParameter("q");
            if (searchQ == null) {
                searchQ = "";
            }
            Integer areaFilterId = AllocationStageHelper.parseAreaFilter(request.getParameter("areaFilter"));
            int page = AllocationStageHelper.parsePage(request.getParameter("page"));
            int pageSize = AllocationStageHelper.parsePageSize(request.getParameter("size"));
            if (urlSessionId > 0 && session != null) {
                Integer allocationPageSession = (Integer) session.getAttribute("allocationPageSessionId");
                if (allocationPageSession != null && allocationPageSession > 0
                        && allocationPageSession != urlSessionId) {
                    page = 1;
                }
                session.setAttribute("allocationPageSessionId", urlSessionId);
            }
            ExamRegistrationSort.Spec sortSpec = ExamRegistrationSort.parse(
                    request.getParameter("sort"), request.getParameter("dir"));
            request.setAttribute("sortBy", sortSpec.getColumn());
            request.setAttribute("sortDir", sortSpec.isAscending() ? "asc" : "desc");
            String sessionIdParam = sessionId > 0 ? String.valueOf(sessionId) : request.getParameter("sessionId");

            if (action == null) {
                AllocationActionResultDTO overviewResult = allocationActionService.autoAllocateOnOverview(
                        sessionId, stage);
                if (overviewResult.getAllocatedCount() > 0) {
                    qList = refreshCandidateQueue(session, examId, sessionId, webRoot, pageCtx.getAllSessions());
                    publishCandidateQueue(request, session, qList, examId, sessionId);
                }
            }

            if (action != null) {
                try {
                    if ("autoAllocate".equals(action)) {
                        AllocationActionResultDTO allocResult = allocationActionService.executeAutoAllocate(sessionId);
                        AllocationActionResultBinder.apply(request, session, allocResult, auditLogSupport);
                        if (allocResult.getAllocatedCount() > 0) {
                            qList = refreshCandidateQueue(session, examId, sessionId, webRoot, pageCtx.getAllSessions());
                        }
                        stage = AllocationStageHelper.STAGE_THEORY;
                        servletPath = allocResult.getRedirectServletPath();
                        jspPath = AllocationStageHelper.resolveJspPath(servletPath);
                    } else if (regIdStr != null) {
                        int regId = Integer.parseInt(regIdStr);
                        ExamRegistrationDTO profile = allocationActionService.findCandidate(regId, sessionId, qList);
                        if (profile != null) {
                            AllocationCandidateActionRequest actionRequest = new AllocationCandidateActionRequest();
                            actionRequest.setAction(action);
                            actionRequest.setRegId(regId);
                            actionRequest.setSessionId(sessionId);
                            actionRequest.setProfile(profile);
                            if ("allocateRoom".equals(action) || "allocatePracticalRoom".equals(action)) {
                                actionRequest.setAreaId(Integer.parseInt(request.getParameter("areaId")));
                            }
                            AllocationActionResultDTO actionResult = allocationActionService.executeCandidateAction(
                                    actionRequest);
                            AllocationActionResultBinder.apply(request, session, actionResult, auditLogSupport);
                            if (actionResult.isSyncCallBoard()) {
                                boolean shiftEnded = "true".equals(session.getAttribute("shiftEnded"));
                                callBoardHttp.sync(getServletContext(),
                                        sessionId, actionResult.getCallingSbd(), null, shiftEnded);
                            }
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
                qList = refreshCandidateQueue(session, examId, sessionId, webRoot, pageCtx.getAllSessions());
                publishCandidateQueue(request, session, qList, examId, sessionId);
                session.setAttribute("lastLoadedSessionId", sessionId);

                if (shouldRedirectAfterAction(action)) {
                    stashAllocationFlash(session, request);
                    response.sendRedirect(buildRedirectUrl(request, servletPath, sessionId, page, pageSize,
                            searchQ, sortSpec, areaFilterId));
                    return;
                }
            }

            ExamStaffHttpSupport.consumeFlash(session, "allocationFlashMsg", request, "alertMsg");
            ExamStaffHttpSupport.consumeFlash(session, "allocationFlashError", request, "errorMsg");
            ExamStaffHttpSupport.consumeFlash(session, "allocationFlashWarn", request, "warningMsg");

            CandidateCallBoardStateDTO state = callBoardHttp.getBoardState(
                    getServletContext(), sessionId);
            if (state != null) {
                String callingSbd = (String) session.getAttribute("callingSbd");
                if (callingSbd != null) {
                    state.setCallingSbd(callingSbd);
                }
                state.setShiftEnded("true".equals(session.getAttribute("shiftEnded")));
            }

            publishStageData(request, qList, stage, resultFilter, searchQ, page, pageSize, sortSpec, areaFilterId);

            request.setAttribute("allocationListPath", servletPath);
            request.setAttribute("allocationExtraQuery", AllocationStageHelper.buildExtraQuery(
                    page, pageSize, searchQ, sessionIdParam,
                    sortSpec.getColumn(), sortSpec.isAscending() ? "asc" : "desc", areaFilterId));
            request.setAttribute("allocationSearchQuery", searchQ.trim());
            request.setAttribute("allocationAreaFilter", areaFilterId);
            request.setAttribute("allocationPageSize", pageSize);
            try {
                request.setAttribute("activeTheoryRooms",
                        areaQueryService.listStaffedTheoryRoomsForExam(sessionId > 0 ? sessionId : examId));
                request.setAttribute("activePracticalAreas",
                        areaQueryService.listStaffedPracticalAreasForExam(sessionId > 0 ? sessionId : examId));
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("activeTheoryRooms", List.of());
                request.setAttribute("activePracticalAreas", List.of());
            }

            ExamStaffHttpSupport.consumeFlash(session, "sessionSelectMsg", request, "sessionSelectMsg");
            ExamStaffHttpSupport.consumeFlash(session, "sessionSelectError", request, "sessionSelectError");

            request.getRequestDispatcher(jspPath).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "Không tải được trang phân bổ: " + e.getMessage());
            publishStageData(request, List.of(), stage, resultFilter, "", 1,
                    AllocationStageHelper.DEFAULT_PAGE_SIZE,
                    ExamRegistrationSort.parse(null, null), null);
            request.setAttribute("allocationListPath", servletPath);
            request.setAttribute("allocationSearchQuery", "");
            request.setAttribute("allocationAreaFilter", null);
            request.setAttribute("allocationExtraQuery", "");
            request.setAttribute("allocationPageSize", AllocationStageHelper.DEFAULT_PAGE_SIZE);
            request.setAttribute("activeTheoryRooms", List.of());
            request.setAttribute("activePracticalAreas", List.of());
            try {
                request.getRequestDispatcher(jspPath).forward(request, response);
            } catch (Exception forwardError) {
                forwardError.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Không tải được trang phân bổ: " + e.getMessage());
            }
        }
    }

    private void publishStageData(HttpServletRequest request, List<ExamRegistrationDTO> qList,
            String stage, String resultFilter, String searchQ, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec, Integer areaFilterId) {
        AllocationStageViewBinder.bind(request,
                allocationStageViewService.buildView(
                        qList, stage, resultFilter, searchQ, page, pageSize, sortSpec, areaFilterId));
    }

    private List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, int sessionId,
            String webRoot, List<SessionDTO> allSessions) {
        if (session == null) {
            return List.of();
        }
        ExamStaffQueueRefreshInput input = new ExamStaffQueueRefreshInput();
        input.setExamId(examId);
        input.setSessionId(sessionId);
        input.setWebRoot(webRoot);
        input.setAllSessions(allSessions);
        input.setSelectedSessionId((Integer) session.getAttribute("selectedSessionId"));
        @SuppressWarnings("unchecked")
        List<String> order = (List<String>) session.getAttribute("callQueueOrder");
        input.setCallQueueOrder(order);
        input.setCallQueueOrderSessionId((Integer) session.getAttribute("callQueueOrderSessionId"));

        CandidateQueueSnapshotDTO snapshot = candidateQueueService.refreshQueue(input);
        ExamStaffPageBinder.publishQueue(null, session, snapshot);
        return snapshot.getFullQueue();
    }

    private void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, int examId, int sessionId) {
        CandidateQueueSnapshotDTO snapshot = candidateQueueService.buildSnapshot(qList, examId, sessionId);
        SessionDTO current = selectionFacade.findSessionById(selectionFacade.loadAllSessions(), sessionId);
        if (current == null && examId > 0) {
            current = selectionFacade.representativeSessionForExam(
                    selectionFacade.loadAllSessions(), examId);
        }
        ExamStaffPageBinder.publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), examId, sessionId, current);
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

    private static String buildRedirectUrl(HttpServletRequest request, String servletPath, int sessionId,
            int page, int pageSize, String searchQ, ExamRegistrationSort.Spec sortSpec, Integer areaFilterId) {
        String extra = AllocationStageHelper.buildExtraQuery(page, pageSize, searchQ,
                sessionId > 0 ? String.valueOf(sessionId) : null,
                sortSpec.getColumn(), sortSpec.isAscending() ? "asc" : "desc", areaFilterId);
        StringBuilder url = new StringBuilder(request.getContextPath()).append(servletPath);
        if (extra != null && !extra.isBlank()) {
            url.append('?').append(extra.startsWith("&") ? extra.substring(1) : extra);
        } else if (sessionId > 0) {
            url.append("?sessionId=").append(sessionId);
        } else {
            url.append("?_=").append(System.currentTimeMillis());
            return url.toString();
        }
        url.append("&_=").append(System.currentTimeMillis());
        return url.toString();
    }
}
