package controller.staff.exam;
import dto.ServiceResult;
import dto.payload.AutoAllocateData;
import dto.payload.UpdateAllocatedRoomCommand;
import dto.payload.UpdateEnrollmentScoresCommand;
import dto.payload.UpdateRoadScoreCommand;
import dto.CandidateCallBoardStateDTO;
import dto.CandidateEnrollmentDTO;
import dto.SessionDTO;
import enums.AuditAction;
import enums.AuditEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.ExamArea;
import model.User;
import service.AuditLogService;
import service.CandidatePhotoService;
import service.ExamAreaService;
import service.ExamRegistrationService;
import service.ExamSessionControlService;
import service.ExaminerAllocationService;
import service.impl.AuditLogServiceImpl;
import service.impl.CandidatePhotoServiceImpl;
import service.impl.ExamAreaServiceImpl;
import service.impl.ExamRegistrationServiceImpl;
import service.impl.ExamSessionControlServiceImpl;
import service.impl.ExaminerAllocationServiceImpl;
@WebServlet("/views/staff/exam/allocation")
public class AllocationServlet extends HttpServlet {
    private static final String CALL_BOARD_CONTEXT_KEY = "candidateCallBoards";
    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    private final ExamRegistrationService regService = new ExamRegistrationServiceImpl();
    private final ExamAreaService areaService = new ExamAreaServiceImpl();
    private final ExamSessionControlService sessionService = new ExamSessionControlServiceImpl();
    private final ExaminerAllocationService allocationService = new ExaminerAllocationServiceImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        request.removeAttribute("errorMsg");
        request.removeAttribute("warningMsg");
        request.removeAttribute("alertMsg");
        // 0. Load all sessions for session dropdown
        List<SessionDTO> allSessions = sessionService.getAllSessions();
        request.setAttribute("allSessions", allSessions);
        // 1. Retrieve or load selected sessionId
        String sessIdParam = request.getParameter("sessionId");
        int sessionId = 2; // Default session
        if (sessIdParam != null && !sessIdParam.isEmpty()) {
            try {
                sessionId = Integer.parseInt(sessIdParam);
            } catch (Exception e) {
            }
        } else if (session.getAttribute("selectedSessionId") != null) {
            sessionId = (Integer) session.getAttribute("selectedSessionId");
        }
        session.setAttribute("selectedSessionId", sessionId);
        // Retrieve the current session details
        SessionDTO currentSession = null;
        for (SessionDTO s : allSessions) {
            if (s.getId() == sessionId) {
                currentSession = s;
                break;
            }
        }
        request.setAttribute("currentSession", currentSession);
        // Load queue for this session if session changed or first time
        List<CandidateEnrollmentDTO> qList = (List<CandidateEnrollmentDTO>) session.getAttribute("candidateQueue");
        Integer lastLoadedSessId = (Integer) session.getAttribute("lastLoadedSessionId");
        if (qList == null || lastLoadedSessId == null || lastLoadedSessId != sessionId) {
            qList = regService.getCandidatesBySession(sessionId);
            session.setAttribute("candidateQueue", qList);
            session.setAttribute("lastLoadedSessionId", sessionId);
        }
        // 3. Handle pipeline action triggers
        String action = request.getParameter("action");
        String regIdStr = request.getParameter("id");
        if (action != null) {
            try {
                if ("autoAllocate".equals(action)) {
                    ServiceResult<AutoAllocateData> allocResult = allocationService.autoAllocateSession(sessionId);
                    if (!allocResult.isSuccess()) {
                        request.setAttribute("errorMsg", allocResult.getMessage());
                    } else if (allocResult.getData() != null
                            && allocResult.getData().getWarningMessage() != null) {
                        request.setAttribute("warningMsg", allocResult.getData().getWarningMessage());
                    }
                    int allocatedCount = allocResult.getData() != null
                            ? allocResult.getData().getAllocatedCount() : 0;
                    if (allocatedCount > 0) {
                        request.setAttribute("alertMsg", "");
                        addAuditLog(session, AuditAction.UPDATE, AuditEntity.CANDIDATE, "");
                        qList = regService.getCandidatesBySession(sessionId);
                        session.setAttribute("candidateQueue", qList);
                        session.setAttribute("lastLoadedSessionId", sessionId);
                    } else if (allocResult.isSuccess()) {
                        request.setAttribute("warningMsg", "");
                    }
                } else if (regIdStr != null) {
                    int regId = Integer.parseInt(regIdStr);
                    // Find matching profile in session
                    CandidateEnrollmentDTO profile = null;
                    for (CandidateEnrollmentDTO c : qList) {
                        if (c.getId() == regId) {
                            profile = c;
                            break;
                        }
                    }
                    if (profile != null) {
                        if ("checkin".equals(action)) {
                            boolean ok = regService.updatePresent(regId, true).isSuccess();
                            if (ok) {
                                profile.setIsPresent(true);
                            }
                        } else if ("callCandidate".equals(action)) {
                            session.setAttribute("callingSbd", String.valueOf(profile.getSbd()));
                            CandidateCallBoardStateDTO state = getCallBoardState(sessionId);
                            if (state != null) {
                                state.setCallingSbd(String.valueOf(profile.getSbd()));
                                state.setShiftEnded("true".equals(session.getAttribute("shiftEnded")));
                            }
                        } else if ("allocateRoom".equals(action)) {
                            int areaId = Integer.parseInt(request.getParameter("areaId"));
                            ExamArea targetArea = areaService.getById(areaId);
                            if (targetArea != null && profile.getAllocatedAreaId() != areaId) {
                                UpdateAllocatedRoomCommand roomCommand = new UpdateAllocatedRoomCommand();
                                roomCommand.setCandidateId(regId);
                                roomCommand.setAreaId(targetArea.getExamAreaId());
                                roomCommand.setAreaName(targetArea.getAreaName());
                                boolean ok = regService.updateAllocatedRoom(roomCommand).isSuccess();
                                if (ok) {
                                    profile.setAllocatedAreaId(targetArea.getExamAreaId());
                                    profile.setAllocatedAreaName(targetArea.getAreaName());
                                    profile.setNotes("AllocatedRoom:" + targetArea.getExamAreaId() + ":" + targetArea.getAreaName());
                                    addAuditLog(session, AuditAction.UPDATE, AuditEntity.CANDIDATE,
                                            "" + targetArea.getAreaName() + " cho SBD " + profile.getSbd(),
                                            regId);
                                }
                            }
                        } else if ("submitTheoryScore".equals(action)) {
                            int score = Integer.parseInt(request.getParameter("score"));
                            String passed = score >= 80 ? "passed" : "failed";
                            Integer oldScore = profile.getTheoryScore();
                            if (oldScore == null || oldScore != score) {
                                UpdateEnrollmentScoresCommand scoresCommand = new UpdateEnrollmentScoresCommand();
                                scoresCommand.setCandidateId(regId);
                                scoresCommand.setTheoryScore(score);
                                scoresCommand.setTheoryResult(passed);
                                boolean ok = regService.updateScores(scoresCommand).isSuccess();
                                if (ok) {
                                    profile.setTheoryScore(score);
                                    profile.setTheoryPassed(passed);
                                    addAuditLog(session, AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                                            "" + score + " - " + passed.toUpperCase() + " cho SBD " + profile.getSbd(),
                                            regId);
                                } else {
                                    request.setAttribute("errorMsg", "");
                                }
                            }
                        } else if ("submitPracticalScore".equals(action)) {
                            int score = Integer.parseInt(request.getParameter("score"));
                            String passed = score >= 80 ? "passed" : "failed";
                            Integer oldScore = profile.getPracticalScore();
                            if (oldScore == null || oldScore != score) {
                                UpdateEnrollmentScoresCommand scoresCommand = new UpdateEnrollmentScoresCommand();
                                scoresCommand.setCandidateId(regId);
                                scoresCommand.setPracticalScore(score);
                                scoresCommand.setPracticalResult(passed);
                                boolean ok = regService.updateScores(scoresCommand).isSuccess();
                                if (ok) {
                                    profile.setPracticalScore(score);
                                    profile.setPracticalPassed(passed);
                                    addAuditLog(session, AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                                            "" + score + "  -  " + passed.toUpperCase() + " cho SBD " + profile.getSbd(),
                                            regId);
                                } else {
                                    request.setAttribute("errorMsg", "");
                                }
                            }
                        } else if ("submitRoadScore".equals(action)) {
                            int score = Integer.parseInt(request.getParameter("score"));
                            String passed = score >= 80 ? "passed" : "failed";
                            Integer oldScore = profile.getRoadTestScore();
                            if (oldScore == null || oldScore != score) {
                                UpdateRoadScoreCommand roadCommand = new UpdateRoadScoreCommand();
                                roadCommand.setCandidateId(regId);
                                roadCommand.setScore(score);
                                roadCommand.setPassed(passed);
                                boolean ok = regService.updateRoadScore(roadCommand).isSuccess();
                                if (ok) {
                                    profile.setRoadTestScore(score);
                                    profile.setRoadTestPassed(passed);
                                    addAuditLog(session, AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                                            "" + score + "  - " + passed.toUpperCase() + " cho SBD " + profile.getSbd(),
                                            regId);
                                } else {
                                    request.setAttribute("errorMsg", "" + profile.getSbd() + ".");
                                }
                            }
                        } else if ("quickComplete".equals(action)) {
                            // Simulates complete desk registrations (photo + payment)
                            String photoPath = "assets/imgs/candidates/" + profile.getSbd() + "_captured.png";
                            regService.updatePhoto(regId, photoPath);
                            regService.updatePayment(regId, true);
                            regService.updatePresent(regId, true);
                            profile.setPhotoUrl(photoPath);
                            profile.setIsPaymentCompleted(true);
                            profile.setIsPresent(true);
                            addAuditLog(session, AuditAction.UPDATE, AuditEntity.CANDIDATE, "" + profile.getSbd());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 4. Reload all database state after actions to ensure 100% request/response synchronization
        qList = regService.getCandidatesBySession(sessionId);
        photoService.normalizeQueue(request.getServletContext().getRealPath("/"), qList);
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedSessionId", sessionId);
        CandidateCallBoardStateDTO state = getCallBoardState(sessionId);
        if (state != null) {
            String callingSbd = (String) session.getAttribute("callingSbd");
            if (callingSbd != null) {
                state.setCallingSbd(callingSbd);
            }
            state.setShiftEnded("true".equals(session.getAttribute("shiftEnded")));
        }
        request.setAttribute("activeTheoryRooms", areaService.getActiveTheoryRooms());
        request.getRequestDispatcher("/views/staff/exam/allocation.jsp").forward(request, response);
    }
    private void addAuditLog(HttpSession session, AuditAction action, AuditEntity entity, String details) {
        addAuditLog(session, action, entity, details, 0);
    }
    private void addAuditLog(HttpSession session, AuditAction action, AuditEntity entity, String details, int recordId) {
        try {
            auditLogService.logAction(((User) session.getAttribute("user")).getUserId(), action, entity, details, recordId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    @SuppressWarnings("unchecked")
    private CandidateCallBoardStateDTO getCallBoardState(int examSessionId) {
        if (examSessionId <= 0) {
            return null;
        }
        jakarta.servlet.ServletContext ctx = getServletContext();
        Map<Integer, CandidateCallBoardStateDTO> boards =
                (Map<Integer, CandidateCallBoardStateDTO>) ctx.getAttribute(CALL_BOARD_CONTEXT_KEY);
        if (boards == null) {
            synchronized (ctx) {
                boards = (Map<Integer, CandidateCallBoardStateDTO>) ctx.getAttribute(CALL_BOARD_CONTEXT_KEY);
                if (boards == null) {
                    boards = new HashMap<>();
                    ctx.setAttribute(CALL_BOARD_CONTEXT_KEY, boards);
                }
            }
        }
        return boards.computeIfAbsent(examSessionId, id -> new CandidateCallBoardStateDTO());
    }
}
