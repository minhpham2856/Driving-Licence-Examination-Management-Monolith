package controller.staff.exam;

import service.ExamRegistrationService;
import service.impl.ExamRegistrationServiceImpl;
import dao.ExamAreaDAO;
import dao.impl.ExamAreaDAOImpl;
import dto.exam.ExamRegistrationDTO;
import dto.SessionDTO;
import model.ExamArea;
import dao.ExamSessionDAO;
import dao.impl.ExamSessionDAOImpl;
import service.ExaminerAllocationService;
import service.impl.ExaminerAllocationServiceImpl;
import service.CandidateCallBoardService;
import service.impl.CandidateCallBoardServiceImpl;
import dto.AutoAllocateResultDTO;
import dto.CandidateCallBoardStateDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import util.ExamRegistrationSort;

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

    private final ExamRegistrationService regDAO = new ExamRegistrationServiceImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

    // Xu ly yeu cau GET
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

            ExamStaffViewHelper.applyNoCacheHeaders(response);
            String webRoot = request.getServletContext().getRealPath("/");

            int urlSessionId = ExamStaffViewHelper.parseSessionIdParam(request);
            if (Boolean.TRUE.equals(session.getAttribute("examStaffSessionJustChanged"))) {
                session.removeAttribute("examStaffSessionJustChanged");
                ExamStaffViewHelper.clearCandidateCache(session);
            }
            if (urlSessionId > 0) {
                Integer loadedSession = (Integer) session.getAttribute("examStaffLoadedSessionId");
                Integer loadedExam = (Integer) session.getAttribute("examStaffLoadedExamId");
                if (loadedSession == null || loadedSession != urlSessionId) {
                    ExamStaffViewHelper.clearCandidateCache(session);
                } else if (loadedExam != null && loadedExam > 0) {
                    SessionDTO urlSession = ExamStaffViewHelper.resolveSessionById(
                            urlSessionId, ExamStaffViewHelper.loadAllSessions(sessionDAO), sessionDAO);
                    if (urlSession != null && urlSession.getExamId() > 0
                            && urlSession.getExamId() != loadedExam) {
                        ExamStaffViewHelper.clearCandidateCache(session);
                    }
                }
                ExamStaffViewHelper.applySessionIdFromRequest(request, session,
                        ExamStaffViewHelper.loadAllSessions(sessionDAO), sessionDAO);
            }

            ExamStaffViewHelper.ExamStaffPageContext pageCtx = ExamStaffViewHelper.prepareExamStaffPage(
                    request, session, sessionDAO, webRoot);
            int examId = pageCtx.getExamId();
            int sessionId = pageCtx.getSessionId();
            List<ExamRegistrationDTO> qList = new ArrayList<>(pageCtx.getCandidates());
            ExamStaffViewHelper.publishCandidateQueue(request, session, qList, examId, sessionId);
            session.setAttribute("lastLoadedSessionId", sessionId);
            request.setAttribute("allocationActiveSessionId", sessionId);

            String action = request.getParameter("action");
            String regIdStr = request.getParameter("id");
            String searchQ = request.getParameter("q");
            if (searchQ == null) {
                searchQ = "";
            }
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

            if (action != null) {
                try {
                    if ("autoAllocate".equals(action)) {
                        ExaminerAllocationService allocationService = new ExaminerAllocationServiceImpl();
                        AutoAllocateResultDTO allocResult = allocationService.autoAllocateSession(sessionId);
                        if (allocResult.errorMsg != null) {
                            request.setAttribute("errorMsg", allocResult.errorMsg);
                        } else if (allocResult.warningMsg != null) {
                            request.setAttribute("warningMsg", allocResult.warningMsg);
                        }
                        if (allocResult.allocatedCount > 0) {
                            request.setAttribute("alertMsg",
                                    "Tự động phân bổ thành công " + allocResult.allocatedCount
                                            + " thí sinh vào phòng thi lý thuyết!");
                            addAuditLog(session, "ALLOCATE Candidates",
                                    "Tự động phân bổ " + allocResult.allocatedCount + " thí sinh vào phòng thi lý thuyết.");
                            qList = ExamStaffViewHelper.refreshCandidateQueue(session, examId, sessionId, webRoot,
                                    pageCtx.getAllSessions());
                        } else if (allocResult.errorMsg == null) {
                            request.setAttribute("warningMsg",
                                    "Không có thí sinh nào đã hoàn thành thủ tục hồ sơ cần phân phòng!");
                        }
                        stage = AllocationStageHelper.STAGE_THEORY;
                        servletPath = "/views/staff/examstaff/allocation-theory";
                        jspPath = AllocationStageHelper.resolveJspPath(servletPath);
                    } else if (regIdStr != null) {
                        int regId = Integer.parseInt(regIdStr);
                        ExamRegistrationDTO profile = null;
                        if (qList != null) {
                            for (ExamRegistrationDTO c : qList) {
                                if (c.getId() == regId) {
                                    profile = c;
                                    break;
                                }
                            }
                        }
                        if (profile != null) {
                            handleCandidateAction(request, session, sessionId, regId, action, profile);
                            String returnPath = AllocationStageHelper.inferServletPathFromAction(action);
                            servletPath = returnPath;
                            stage = AllocationStageHelper.resolveStageFromServletPath(returnPath);
                            resultFilter = AllocationStageHelper.resolveResultFilterFromServletPath(returnPath);
                            jspPath = AllocationStageHelper.resolveJspPath(returnPath);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setAttribute("errorMsg", "Lỗi xử lý: " + e.getMessage());
                }

                qList = ExamStaffViewHelper.refreshCandidateQueue(session, examId, sessionId, webRoot,
                        pageCtx.getAllSessions());
                ExamStaffViewHelper.publishCandidateQueue(request, session, qList, examId, sessionId);
                session.setAttribute("lastLoadedSessionId", sessionId);
            }

            CandidateCallBoardService callBoardService = new CandidateCallBoardServiceImpl();
            CandidateCallBoardStateDTO state = callBoardService.getState(getServletContext(), sessionId);
            if (state != null) {
                String callingSbd = (String) session.getAttribute("callingSbd");
                if (callingSbd != null) {
                    state.setCallingSbd(callingSbd);
                }
                state.setShiftEnded("true".equals(session.getAttribute("shiftEnded")));
            }

            publishStageData(request, qList, stage, resultFilter, searchQ, page, pageSize, sortSpec);

            request.setAttribute("allocationListPath", servletPath);
            request.setAttribute("allocationExtraQuery", AllocationStageHelper.buildExtraQuery(
                    page, pageSize, searchQ, sessionIdParam,
                    sortSpec.getColumn(), sortSpec.isAscending() ? "asc" : "desc"));
            request.setAttribute("allocationSearchQuery", searchQ.trim());
            request.setAttribute("allocationPageSize", pageSize);
            try {
                request.setAttribute("activeTheoryRooms", areaDAO.getActiveTheoryRooms());
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("activeTheoryRooms", List.of());
            }

            ExamStaffViewHelper.consumeFlash(session, "sessionSelectMsg", request, "sessionSelectMsg");
            ExamStaffViewHelper.consumeFlash(session, "sessionSelectError", request, "sessionSelectError");

            request.getRequestDispatcher(jspPath).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "Không tải được trang phân bổ: " + e.getMessage());
            publishStageData(request, List.of(), stage, resultFilter, "", 1,
                    AllocationStageHelper.DEFAULT_PAGE_SIZE,
                    ExamRegistrationSort.parse(null, null));
            request.setAttribute("allocationListPath", servletPath);
            request.setAttribute("allocationSearchQuery", "");
            request.setAttribute("allocationExtraQuery", "");
            request.setAttribute("allocationPageSize", AllocationStageHelper.DEFAULT_PAGE_SIZE);
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

    private void handleCandidateAction(HttpServletRequest request, HttpSession session,
            int sessionId, int regId, String action, ExamRegistrationDTO profile) throws Exception {
        if ("checkin".equals(action)) {
            if (regDAO.updatePresent(regId, true)) {
                profile.setIsPresent(true);
            }
        } else if ("callCandidate".equals(action)) {
            session.setAttribute("callingSbd", profile.getSbd());
            CandidateCallBoardService callBoardService = new CandidateCallBoardServiceImpl();
            CandidateCallBoardStateDTO state = callBoardService.getState(getServletContext(), sessionId);
            if (state != null) {
                state.setCallingSbd(profile.getSbd());
                state.setShiftEnded("true".equals(session.getAttribute("shiftEnded")));
            }
        } else if ("allocateRoom".equals(action)) {
            int areaId = Integer.parseInt(request.getParameter("areaId"));
            ExamArea targetArea = areaDAO.getById(areaId);
            if (targetArea != null && profile.getAllocatedAreaId() != areaId) {
                if (regDAO.updateAllocatedRoom(regId, targetArea.getId(), targetArea.getAreaName())) {
                    profile.setAllocatedAreaId(targetArea.getId());
                    profile.setAllocatedAreaName(targetArea.getAreaName());
                    profile.setNotes("AllocatedRoom:" + targetArea.getId() + ":" + targetArea.getAreaName());
                    addAuditLog(session, "UPDATE ExamRegistrationDTO",
                            "Chuyển phòng thi → " + targetArea.getAreaName() + " cho SBD " + profile.getSbd(),
                            regId);
                }
            }
        } else if ("submitTheoryScore".equals(action)) {
            int score = Integer.parseInt(request.getParameter("score"));
            String license = AllocationPassRules.normalizeLicense(profile.getLicenseCode(), profile.getClazz());
            boolean theoryOk = AllocationPassRules.isTheoryPassed(license, score);
            String passed = AllocationPassRules.toPassFlag(theoryOk);
            Integer oldScore = profile.getTheoryScore();
            if (oldScore == null || oldScore != score) {
                if (regDAO.updateScores(regId, score, passed, null, null)) {
                    profile.setTheoryScore(score);
                    profile.setTheoryPassed(passed);
                    int need = AllocationPassRules.theoryMinCorrect(license);
                    int total = AllocationPassRules.theoryQuestionTotal(license);
                    String auditDetail = "Nhập điểm LÝ THUYẾT: " + score + "/" + total
                            + " (đạt ≥" + need + ") → " + passed.toUpperCase()
                            + " cho SBD " + profile.getSbd();
                    if (theoryOk && profile.skipsPractical()) {
                    // add audit log
                        auditDetail += " — bảo lưu thực hành/sa hình"
                                + (profile.skipsRoad() ? " và đường trường" : "")
                                + ", đỗ kỳ thi";
                    }
                    addAuditLog(session, "UPDATE ExamScore", auditDetail, regId);
                } else {
                    request.setAttribute("errorMsg",
                            "Không lưu được điểm lý thuyết cho SBD " + profile.getSbd()
                                    + ". Kiểm tra ExamEnrollment và Session_ExamSection.");
                }
            }
        } else if ("submitPracticalScore".equals(action)) {
            int score = Integer.parseInt(request.getParameter("score"));
            String passed = AllocationPassRules.toPassFlag(AllocationPassRules.isPracticalPassed(score));
            Integer oldScore = profile.getPracticalScore();
            if (oldScore == null || oldScore != score) {
                if (regDAO.updateScores(regId, null, null, score, passed)) {
                    profile.setPracticalScore(score);
                    profile.setPracticalPassed(passed);
                    addAuditLog(session, "UPDATE ExamScore",
                            "Nhập điểm THỰC HÀNH: " + score + " → " + passed.toUpperCase()
                                    + " cho SBD " + profile.getSbd(),
                            regId);
                } else {
                    request.setAttribute("errorMsg",
                            "Không lưu được điểm thực hành/sa hình cho SBD " + profile.getSbd()
                                    + ". Kiểm tra ExamEnrollment và Session_ExamSection.");
                }
            }
        } else if ("submitRoadScore".equals(action)) {
            int score = Integer.parseInt(request.getParameter("score"));
            String passed = AllocationPassRules.toPassFlag(AllocationPassRules.isRoadPassed(score));
            Integer oldScore = profile.getRoadTestScore();
            if (oldScore == null || oldScore != score) {
                if (regDAO.updateRoadScore(regId, score, passed)) {
                    profile.setRoadTestScore(score);
                    profile.setRoadTestPassed(passed);
                    addAuditLog(session, "UPDATE ExamScore",
                            "Nhập điểm ĐƯỜNG TRƯỜNG: " + score + " → " + passed.toUpperCase()
                                    + " cho SBD " + profile.getSbd(),
                            regId);
                } else {
                    request.setAttribute("errorMsg",
                            "Không lưu được điểm đường trường cho SBD " + profile.getSbd() + ".");
                }
            }
        } else if ("quickComplete".equals(action)) {
            String photoPath = "assets/imgs/candidates/" + profile.getSbd() + "_captured.png";
            regDAO.updatePhoto(regId, photoPath);
            regDAO.updatePayment(regId, true);
            regDAO.updatePresent(regId, true);
            profile.setPhotoUrl(photoPath);
            profile.setIsPaymentCompleted(true);
            profile.setIsPresent(true);
    // publish stage data
            addAuditLog(session, "UPDATE ExamRegistrationDTO",
                    "Hoàn thành nhanh thủ tục (FaceID + lệ phí) cho SBD " + profile.getSbd());
        }
    }

    private void publishStageData(HttpServletRequest request, List<ExamRegistrationDTO> qList,
            String stage, String resultFilter, String searchQ, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec) {
        if (qList == null) {
            request.setAttribute("allocationPracticalStageIds", Set.of());
            request.setAttribute("allocationNoRoadTestIds", Set.of());
            request.setAttribute("allocationStageCounts", new AllocationStageHelper.StageCounts());
            request.setAttribute("allocationStageList", List.of());
            request.setAttribute("allocationPageSlice",
                    new AllocationStageHelper.PageSlice<>(List.of(), page, pageSize, 0));
            return;
        }

        Set<Integer> practicalStageIds = new HashSet<>();
        Set<Integer> noRoadTestIds = new HashSet<>();
        for (ExamRegistrationDTO c : qList) {
            AllocationPassRules.applyToCandidate(c);
            if (AllocationPassRules.isPracticalStageEligible(c)) {
                practicalStageIds.add(c.getId());
            }
            String license = AllocationPassRules.normalizeLicense(c.getLicenseCode(), c.getClazz());
            if (!AllocationPassRules.requiresRoadTest(license) || c.skipsRoad()) {
                noRoadTestIds.add(c.getId());
            }
        }
        request.setAttribute("allocationPracticalStageIds", practicalStageIds);
        request.setAttribute("allocationNoRoadTestIds", noRoadTestIds);
        request.setAttribute("allocationStageCounts",
                AllocationStageHelper.computeCounts(qList, practicalStageIds));

        List<ExamRegistrationDTO> stageFiltered = new ArrayList<>();
        if (!AllocationStageHelper.STAGE_OVERVIEW.equals(stage)) {
            String filter = AllocationStageHelper.STAGE_RESULTS.equals(stage) ? resultFilter : null;
            stageFiltered = AllocationStageHelper.filterForStage(qList, stage, practicalStageIds, filter);
            stageFiltered = AllocationStageHelper.filterSearch(stageFiltered, searchQ);
        }
        ExamRegistrationSort.sort(stageFiltered, sortSpec);
    // add audit log
        // add audit log
        AllocationStageHelper.PageSlice<ExamRegistrationDTO> slice
                = AllocationStageHelper.paginate(stageFiltered, page, pageSize);
    // add audit log
        request.setAttribute("allocationStageList", slice.getItems());
        request.setAttribute("allocationPageSlice", slice);
    }

    private void addAuditLog(HttpSession session, String action, String details) {
        addAuditLog(session, action, details, 0);
    }
    // Xu ly yeu cau POST

    private void addAuditLog(HttpSession session, String action, String details, int recordId) {
        // Xu ly yeu cau GET
        try {
            util.AuditLogHelper.persist(session, action, details, recordId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
