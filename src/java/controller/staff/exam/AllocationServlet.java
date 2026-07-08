package controller.staff.exam;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.AllocationActionResultDTO;
import dto.examstaff.AllocationCandidateActionRequest;
import dto.examstaff.CandidateCallBoardStateDTO;
import dto.SessionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import repository.ServletContextCallBoardRepository;
import service.AllocationActionService;
import service.AllocationStageViewService;
import service.CandidateCallBoardService;
import service.ExamAreaQueryService;
import service.ExamStaffServices;
import util.ExamRegistrationSort;
import util.examstaff.AllocationStageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {
        "/views/staff/examstaff/allocation",
        "/views/staff/examstaff/allocation-waiting",
        "/views/staff/examstaff/allocation-theory",
        "/views/staff/examstaff/allocation-practical",
        "/views/staff/examstaff/allocation-road",
        "/views/staff/examstaff/allocation-results-pass",
        "/views/staff/examstaff/allocation-results-fail"
})
public class AllocationServlet extends HttpServlet {

    private final ExamAreaQueryService areaQueryService;
    private final CandidateCallBoardService callBoardService;
    private final AllocationStageViewService allocationStageViewService;
    private final AllocationActionService allocationActionService;

    public AllocationServlet() {
        this(ExamStaffServices.get().examAreas(), ExamStaffServices.get().callBoard(),
                ExamStaffServices.get().allocationStageView(), ExamStaffServices.get().allocationActions());
    }

    AllocationServlet(ExamAreaQueryService areaQueryService,
            CandidateCallBoardService callBoardService,
            AllocationStageViewService allocationStageViewService,
            AllocationActionService allocationActionService) {
        this.areaQueryService = areaQueryService;
        this.callBoardService = callBoardService;
        this.allocationStageViewService = allocationStageViewService;
        this.allocationActionService = allocationActionService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String servletPath = request.getServletPath();
        String stage = AllocationStageUtil.resolveStageFromServletPath(servletPath);
        String resultFilter = AllocationStageUtil.resolveResultFilterFromServletPath(servletPath);
        String jspPath = AllocationStageUtil.resolveJspPath(servletPath);

        try {
            request.removeAttribute("errorMsg");
            request.removeAttribute("warningMsg");
            request.removeAttribute("alertMsg");

            BaseExamStaffServlet.applyNoCacheHeaders(response);
            String webRoot = request.getServletContext().getRealPath("/");

            int urlSessionId = BaseExamStaffServlet.parseSessionIdParam(request);
            if (Boolean.TRUE.equals(session.getAttribute("examStaffSessionJustChanged"))) {
                session.removeAttribute("examStaffSessionJustChanged");
                BaseExamStaffServlet.clearCandidateCache(session);
            }
            if (urlSessionId > 0) {
                Integer loadedSession = (Integer) session.getAttribute("examStaffLoadedSessionId");
                Integer loadedExam = (Integer) session.getAttribute("examStaffLoadedExamId");
                if (loadedSession == null || loadedSession != urlSessionId) {
                    BaseExamStaffServlet.clearCandidateCache(session);
                } else if (loadedExam != null && loadedExam > 0) {
                    SessionDTO urlSession = BaseExamStaffServlet.resolveSessionById(
                            urlSessionId, BaseExamStaffServlet.loadAllSessions());
                    if (urlSession != null && urlSession.getExamId() > 0
                            && urlSession.getExamId() != loadedExam) {
                        BaseExamStaffServlet.clearCandidateCache(session);
                    }
                }
                BaseExamStaffServlet.applySessionIdFromRequest(request, session,
                        BaseExamStaffServlet.loadAllSessions());
            }

            BaseExamStaffServlet.ExamStaffPageContext pageCtx = BaseExamStaffServlet.prepareExamStaffPage(
                    request, session, webRoot);
            int examId = pageCtx.getExamId();
            int sessionId = pageCtx.getSessionId();
            List<ExamRegistrationDTO> qList = new ArrayList<>(pageCtx.getCandidates());
            BaseExamStaffServlet.publishCandidateQueue(request, session, qList, examId, sessionId);
            session.setAttribute("lastLoadedSessionId", sessionId);
            request.setAttribute("allocationActiveSessionId", sessionId);

            String action = request.getParameter("action");
            String regIdStr = request.getParameter("id");
            String searchQ = request.getParameter("q");
            if (searchQ == null) {
                searchQ = "";
            }
            int page = AllocationStageUtil.parsePage(request.getParameter("page"));
            int pageSize = AllocationStageUtil.parsePageSize(request.getParameter("size"));
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
                    qList = BaseExamStaffServlet.refreshCandidateQueue(session, examId, sessionId, webRoot,
                            pageCtx.getAllSessions());
                    BaseExamStaffServlet.publishCandidateQueue(request, session, qList, examId, sessionId);
                }
            }

            if (action != null) {
                try {
                    if ("autoAllocate".equals(action)) {
                        AllocationActionResultDTO allocResult = allocationActionService.executeAutoAllocate(sessionId);
                        BaseExamStaffServlet.apply(request, session, allocResult);
                        if (allocResult.getAllocatedCount() > 0) {
                            qList = BaseExamStaffServlet.refreshCandidateQueue(session, examId, sessionId, webRoot,
                                    pageCtx.getAllSessions());
                        }
                        stage = AllocationStageUtil.STAGE_THEORY;
                        servletPath = allocResult.getRedirectServletPath();
                        jspPath = AllocationStageUtil.resolveJspPath(servletPath);
                    } else if (regIdStr != null) {
                        int regId = Integer.parseInt(regIdStr);
                        ExamRegistrationDTO profile = allocationActionService.findCandidate(regId, sessionId, qList);
                        if (profile != null) {
                            AllocationCandidateActionRequest actionRequest = new AllocationCandidateActionRequest();
                            actionRequest.setAction(action);
                            actionRequest.setRegId(regId);
                            actionRequest.setSessionId(sessionId);
                            actionRequest.setProfile(profile);
                            if ("allocateRoom".equals(action)) {
                                actionRequest.setAreaId(Integer.parseInt(request.getParameter("areaId")));
                            } else if (action != null && action.startsWith("submit") && action.endsWith("Score")) {
                                actionRequest.setScore(Integer.parseInt(request.getParameter("score")));
                            }
                            AllocationActionResultDTO actionResult = allocationActionService.executeCandidateAction(
                                    actionRequest);
                            BaseExamStaffServlet.apply(request, session, actionResult);
                            if (actionResult.isSyncCallBoard()) {
                                boolean shiftEnded = "true".equals(session.getAttribute("shiftEnded"));
                                callBoardService.sync(new ServletContextCallBoardRepository(getServletContext()),
                                        sessionId, actionResult.getCallingSbd(), null, shiftEnded);
                            }
                            servletPath = actionResult.getRedirectServletPath();
                            stage = AllocationStageUtil.resolveStageFromServletPath(servletPath);
                            resultFilter = AllocationStageUtil.resolveResultFilterFromServletPath(servletPath);
                            jspPath = AllocationStageUtil.resolveJspPath(servletPath);
                        } else {
                            request.setAttribute("errorMsg", "Không tìm thấy thí sinh để xử lý.");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setAttribute("errorMsg", "Lỗi xử lý: " + e.getMessage());
                }

                BaseExamStaffServlet.clearCandidateCache(session);
                qList = BaseExamStaffServlet.refreshCandidateQueue(session, examId, sessionId, webRoot,
                        pageCtx.getAllSessions());
                BaseExamStaffServlet.publishCandidateQueue(request, session, qList, examId, sessionId);
                session.setAttribute("lastLoadedSessionId", sessionId);

                if (shouldRedirectAfterAction(action)) {
                    stashAllocationFlash(session, request);
                    response.sendRedirect(buildRedirectUrl(request, servletPath, sessionId, page, pageSize,
                            searchQ, sortSpec));
                    return;
                }
            }

            BaseExamStaffServlet.consumeFlash(session, "allocationFlashMsg", request, "alertMsg");
            BaseExamStaffServlet.consumeFlash(session, "allocationFlashError", request, "errorMsg");
            BaseExamStaffServlet.consumeFlash(session, "allocationFlashWarn", request, "warningMsg");

            CandidateCallBoardStateDTO state = callBoardService.getState(
                    new ServletContextCallBoardRepository(getServletContext()), sessionId);
            if (state != null) {
                String callingSbd = (String) session.getAttribute("callingSbd");
                if (callingSbd != null) {
                    state.setCallingSbd(callingSbd);
                }
                state.setShiftEnded("true".equals(session.getAttribute("shiftEnded")));
            }

            publishStageData(request, qList, stage, resultFilter, searchQ, page, pageSize, sortSpec);

            request.setAttribute("allocationListPath", servletPath);
            request.setAttribute("allocationExtraQuery", AllocationStageUtil.buildExtraQuery(
                    page, pageSize, searchQ, sessionIdParam,
                    sortSpec.getColumn(), sortSpec.isAscending() ? "asc" : "desc"));
            request.setAttribute("allocationSearchQuery", searchQ.trim());
            request.setAttribute("allocationPageSize", pageSize);
            try {
                request.setAttribute("activeTheoryRooms", areaQueryService.listActiveTheoryRooms());
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("activeTheoryRooms", List.of());
            }

            BaseExamStaffServlet.consumeFlash(session, "sessionSelectMsg", request, "sessionSelectMsg");
            BaseExamStaffServlet.consumeFlash(session, "sessionSelectError", request, "sessionSelectError");

            request.getRequestDispatcher(jspPath).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "Không tải được trang phân bổ: " + e.getMessage());
            publishStageData(request, List.of(), stage, resultFilter, "", 1,
                    AllocationStageUtil.DEFAULT_PAGE_SIZE,
                    ExamRegistrationSort.parse(null, null));
            request.setAttribute("allocationListPath", servletPath);
            request.setAttribute("allocationSearchQuery", "");
            request.setAttribute("allocationExtraQuery", "");
            request.setAttribute("allocationPageSize", AllocationStageUtil.DEFAULT_PAGE_SIZE);
            request.setAttribute("activeTheoryRooms", List.of());
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
            ExamRegistrationSort.Spec sortSpec) {
        BaseExamStaffServlet.bind(request,
                allocationStageViewService.buildView(qList, stage, resultFilter, searchQ, page, pageSize, sortSpec));
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
            int page, int pageSize, String searchQ, ExamRegistrationSort.Spec sortSpec) {
        String extra = AllocationStageUtil.buildExtraQuery(page, pageSize, searchQ,
                sessionId > 0 ? String.valueOf(sessionId) : null,
                sortSpec.getColumn(), sortSpec.isAscending() ? "asc" : "desc");
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
