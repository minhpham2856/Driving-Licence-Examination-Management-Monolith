package Controllers.Staff.ExamStaff;

import DAO.ExamRegistrationDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.ExamAreaDAO;
import DAO.Impl.ExamAreaDAOImpl;
import Models.ExamRegistration;
import Models.ExamArea;
import Models.ExamSession;
import DAO.ExamSessionDAO;
import DAO.Impl.ExamSessionDAOImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/views/staff/examstaff/allocation")
public class AllocationServlet extends HttpServlet {

    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        request.removeAttribute("errorMsg");
        request.removeAttribute("warningMsg");
        request.removeAttribute("alertMsg");

        // 0. Load all sessions for session dropdown
        List<ExamSession> allSessions = sessionDAO.getAllSessions();
        request.setAttribute("allSessions", allSessions);

        // 1. Retrieve or load selected sessionId
        String sessIdParam = request.getParameter("sessionId");
        int sessionId = 2; // Default session
        if (sessIdParam != null && !sessIdParam.isEmpty()) {
            try {
                sessionId = Integer.parseInt(sessIdParam);
            } catch (Exception e) {}
        } else if (session.getAttribute("selectedSessionId") != null) {
            sessionId = (Integer) session.getAttribute("selectedSessionId");
        }
        session.setAttribute("selectedSessionId", sessionId);

        // Retrieve the current session details
        ExamSession currentSession = null;
        for (ExamSession s : allSessions) {
            if (s.getId() == sessionId) {
                currentSession = s;
                break;
            }
        }
        request.setAttribute("currentSession", currentSession);

        // Load queue for this session if session changed or first time
        List<ExamRegistration> qList = (List<ExamRegistration>) session.getAttribute("candidateQueue");
        Integer lastLoadedSessId = (Integer) session.getAttribute("lastLoadedSessionId");
        if (qList == null || lastLoadedSessId == null || lastLoadedSessId != sessionId) {
            qList = regDAO.getCandidatesBySession(sessionId);
            session.setAttribute("candidateQueue", qList);
            session.setAttribute("lastLoadedSessionId", sessionId);
        }

        // 3. Handle pipeline action triggers
        String action = request.getParameter("action");
        String regIdStr = request.getParameter("id");
        
        if (action != null) {
            try {
                if ("autoAllocate".equals(action)) {
                    ExamAutoAllocator allocator = new ExamAutoAllocator();
                    ExamAutoAllocator.Result allocResult = allocator.autoAllocateSession(sessionId);
                    if (allocResult.errorMsg != null) {
                        request.setAttribute("errorMsg", allocResult.errorMsg);
                    } else if (allocResult.warningMsg != null) {
                        request.setAttribute("warningMsg", allocResult.warningMsg);
                    }
                    if (allocResult.allocatedCount > 0) {
                        request.setAttribute("alertMsg", "Tự động phân bổ thành công " + allocResult.allocatedCount + " thí sinh vào phòng thi lý thuyết!");
                        addAuditLog(session, "ALLOCATE Candidates", "Tự động phân bổ " + allocResult.allocatedCount + " thí sinh vào phòng thi lý thuyết.");
                        qList = regDAO.getCandidatesBySession(sessionId);
                        session.setAttribute("candidateQueue", qList);
                        session.setAttribute("lastLoadedSessionId", sessionId);
                    } else if (allocResult.errorMsg == null) {
                        request.setAttribute("warningMsg", "Không có thí sinh nào đã hoàn thành thủ tục hồ sơ cần phân phòng!");
                    }

                } else if (regIdStr != null) {
                    int regId = Integer.parseInt(regIdStr);
                    
                    // Find matching profile in session
                    ExamRegistration profile = null;
                    for (ExamRegistration c : qList) {
                        if (c.getId() == regId) {
                            profile = c;
                            break;
                        }
                    }

                    if (profile != null) {
                        if ("checkin".equals(action)) {
                            boolean ok = regDAO.updatePresent(regId, true);
                            if (ok) {
                                profile.setIsPresent(true);
                            }
                        } else if ("callCandidate".equals(action)) {
                            session.setAttribute("callingSbd", profile.getSbd());
                            CandidateCallBoard.sync(getServletContext(), sessionId, profile.getSbd(), qList,
                                    "true".equals(session.getAttribute("shiftEnded")));
                        } else if ("allocateRoom".equals(action)) {
                            int areaId = Integer.parseInt(request.getParameter("areaId"));
                            ExamArea targetArea = areaDAO.getById(areaId);
                            if (targetArea != null && profile.getAllocatedAreaId() != areaId) {
                                boolean ok = regDAO.updateAllocatedRoom(regId, targetArea.getId(), targetArea.getAreaName());
                                if (ok) {
                                    profile.setAllocatedAreaId(targetArea.getId());
                                    profile.setAllocatedAreaName(targetArea.getAreaName());
                                    profile.setNotes("AllocatedRoom:" + targetArea.getId() + ":" + targetArea.getAreaName());
                                    addAuditLog(session, "UPDATE ExamRegistration",
                                            "Chuyển phòng thi → " + targetArea.getAreaName() + " cho SBD " + profile.getSbd(),
                                            regId);
                                }
                            }

                        } else if ("submitTheoryScore".equals(action)) {
                            int score = Integer.parseInt(request.getParameter("score"));
                            String passed = score >= 80 ? "passed" : "failed";
                            Integer oldScore = profile.getTheoryScore();
                            if (oldScore == null || oldScore != score) {
                                boolean ok = regDAO.updateScores(regId, score, passed, null, null);
                                if (ok) {
                                    profile.setTheoryScore(score);
                                    profile.setTheoryPassed(passed);
                                    addAuditLog(session, "UPDATE ExamScore",
                                            "Nhập điểm LÝ THUYẾT: " + score + " → " + passed.toUpperCase() + " cho SBD " + profile.getSbd(),
                                            regId);
                                } else {
                                    request.setAttribute("errorMsg", "Không lưu được điểm lý thuyết cho SBD " + profile.getSbd() + ". Kiểm tra Exam_Candidate và Session_ExamSection.");
                                }
                            }
                        } else if ("submitPracticalScore".equals(action)) {
                            int score = Integer.parseInt(request.getParameter("score"));
                            String passed = score >= 80 ? "passed" : "failed";
                            Integer oldScore = profile.getPracticalScore();
                            if (oldScore == null || oldScore != score) {
                                boolean ok = regDAO.updateScores(regId, null, null, score, passed);
                                if (ok) {
                                    profile.setPracticalScore(score);
                                    profile.setPracticalPassed(passed);
                                    addAuditLog(session, "UPDATE ExamScore",
                                            "Nhập điểm THỰC HÀNH: " + score + " → " + passed.toUpperCase() + " cho SBD " + profile.getSbd(),
                                            regId);
                                } else {
                                    request.setAttribute("errorMsg", "Không lưu được điểm thực hành/sa hình cho SBD " + profile.getSbd() + ". Kiểm tra Exam_Candidate và Session_ExamSection.");
                                }
                            }
                        } else if ("submitRoadScore".equals(action)) {
                            int score = Integer.parseInt(request.getParameter("score"));
                            String passed = score >= 80 ? "passed" : "failed";
                            Integer oldScore = profile.getRoadTestScore();
                            if (oldScore == null || oldScore != score) {
                                boolean ok = regDAO.updateRoadScore(regId, score, passed);
                                if (ok) {
                                    profile.setRoadTestScore(score);
                                    profile.setRoadTestPassed(passed);
                                    addAuditLog(session, "UPDATE ExamScore",
                                            "Nhập điểm ĐƯỜNG TRƯỜNG: " + score + " → " + passed.toUpperCase() + " cho SBD " + profile.getSbd(),
                                            regId);
                                } else {
                                    request.setAttribute("errorMsg", "Không lưu được điểm đường trường cho SBD " + profile.getSbd() + ".");
                                }
                            }
                        } else if ("quickComplete".equals(action)) {
                            // Simulates complete desk registrations (photo + payment)
                            String photoPath = "assets/imgs/candidates/" + profile.getSbd() + "_captured.png";
                            regDAO.updatePhoto(regId, photoPath);
                            regDAO.updatePayment(regId, true);
                            regDAO.updatePresent(regId, true);
                            
                            profile.setPhotoUrl(photoPath);
                            profile.setIsPaymentCompleted(true);
                            profile.setIsPresent(true);
                            addAuditLog(session, "UPDATE ExamRegistration", "Hoàn thành nhanh thủ tục (FaceID + lệ phí) cho SBD " + profile.getSbd());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 4. Reload all database state after actions to ensure 100% request/response synchronization
        qList = regDAO.getCandidatesBySession(sessionId);
        CandidatePhotoHelper.normalizeQueue(request.getServletContext().getRealPath("/"), qList, regDAO);
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedSessionId", sessionId);
        CandidateCallBoard.syncFromSession(getServletContext(), session, qList);

        request.setAttribute("activeTheoryRooms", areaDAO.getActiveTheoryRooms());

        request.getRequestDispatcher("/views/staff/examstaff/allocation.jsp").forward(request, response);
    }

    private void addAuditLog(HttpSession session, String action, String details) {
        addAuditLog(session, action, details, 0);
    }

    private void addAuditLog(HttpSession session, String action, String details, int recordId) {
        try {
            Utils.AuditLogHelper.persist(session, action, details, recordId);
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
