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
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

        LinkedHashMap<Integer, ExamSession> examOptionMap = new LinkedHashMap<>();
        for (ExamSession s : allSessions) {
            if (s.getExamId() > 0 && !examOptionMap.containsKey(s.getExamId())) {
                examOptionMap.put(s.getExamId(), s);
            }
        }
        request.setAttribute("examOptions", new ArrayList<>(examOptionMap.values()));

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

        int examId = (currentSession != null && currentSession.getExamId() > 0) ? currentSession.getExamId() : sessionId;
        request.setAttribute("selectedExamId", examId);

        // Một kỳ thi gồm nhiều ca (lý thuyết → sa hình → đường trường): tải toàn bộ thí sinh theo ExamId
        List<ExamRegistration> qList = (List<ExamRegistration>) session.getAttribute("candidateQueue");
        Integer lastLoadedExamId = (Integer) session.getAttribute("lastLoadedExamId");
        if (qList == null || lastLoadedExamId == null || lastLoadedExamId != examId) {
            qList = regDAO.getCandidatesByExamId(examId);
            session.setAttribute("candidateQueue", qList);
            session.setAttribute("lastLoadedExamId", examId);
        }

        // 3. Handle pipeline action triggers
        String action = request.getParameter("action");
        String regIdStr = request.getParameter("id");
        
        if (action != null) {
            try {
                if ("autoAllocate".equals(action)) {
                    ExamAutoAllocator allocator = new ExamAutoAllocator();
                    Integer theorySessionId = regDAO.resolveSessionIdForSection(examId, "Theory");
                    int allocateSessionId = theorySessionId != null ? theorySessionId : sessionId;
                    ExamAutoAllocator.Result allocResult = allocator.autoAllocateSession(allocateSessionId);
                    if (allocResult.errorMsg != null) {
                        request.setAttribute("errorMsg", allocResult.errorMsg);
                    } else if (allocResult.warningMsg != null) {
                        request.setAttribute("warningMsg", allocResult.warningMsg);
                    }
                    if (allocResult.allocatedCount > 0) {
                        request.setAttribute("alertMsg", "Tự động phân bổ thành công " + allocResult.allocatedCount + " thí sinh vào phòng thi lý thuyết!");
                        addAuditLog(session, "ALLOCATE Candidates", "Tự động phân bổ " + allocResult.allocatedCount + " thí sinh vào phòng thi lý thuyết.");
                        qList = regDAO.getCandidatesByExamId(examId);
                        session.setAttribute("candidateQueue", qList);
                        session.setAttribute("lastLoadedExamId", examId);
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
                                boolean ok = regDAO.updateScoresForExam(regId, examId, score, passed, null, null);
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
                                boolean ok = regDAO.updateScoresForExam(regId, examId, null, null, score, passed);
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
                                boolean ok = regDAO.updateRoadScoreForExam(regId, examId, score, passed);
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
                            String safeSbd = profile.getSbd().replaceAll("[^A-Za-z0-9\\-]", "_");
                            String photoPath = "assets/imgs/candidates/" + safeSbd + "_captured.png";
                            ensurePlaceholderPhoto(request.getServletContext().getRealPath("/"), photoPath);
                            regDAO.updatePhoto(regId, photoPath);
                            regDAO.updatePayment(regId, true);
                            regDAO.updatePresent(regId, true);

                            profile.setPhotoUrl(photoPath);
                            profile.setValidCapturedPhoto(true);
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
        qList = regDAO.getCandidatesByExamId(examId);
        CandidatePhotoHelper.normalizeQueue(request.getServletContext().getRealPath("/"), qList, regDAO);
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedExamId", examId);
        CandidateCallBoard.syncFromSession(getServletContext(), session, qList);

        request.setAttribute("activeTheoryRooms", areaDAO.getActiveTheoryRooms());

        request.getRequestDispatcher("/views/staff/examstaff/allocation.jsp").forward(request, response);
    }

    private void ensurePlaceholderPhoto(String webRoot, String photoPath) {
        if (webRoot == null || photoPath == null || photoPath.isEmpty()) {
            return;
        }
        java.io.File file = new java.io.File(webRoot, photoPath.replace("/", java.io.File.separator));
        if (file.isFile() && file.length() > 0) {
            return;
        }
        java.io.File dir = file.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        byte[] png = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, (byte) 0x1F, 0x15, (byte) 0xC4,
            (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
            0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
            0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00,
            0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
            fos.write(png);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
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
