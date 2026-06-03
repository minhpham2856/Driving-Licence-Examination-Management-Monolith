package Controllers.Staff.ExamStaff;

import DAO.ExamRegistrationDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.ExamComputerDAO;
import DAO.Impl.ExamComputerDAOImpl;
import DAO.ExamDeviceDAO;
import DAO.Impl.ExamDeviceDAOImpl;
import DAO.ExamAreaDAO;
import DAO.Impl.ExamAreaDAOImpl;
import Models.ExamRegistration;
import Models.ExamComputer;
import Models.ExamDevice;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/views/staff/examstaff/allocation")
public class AllocationServlet extends HttpServlet {

    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
    private final ExamComputerDAO compDAO = new ExamComputerDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
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

        // Load active theory rooms for action block usage
        List<ExamArea> activeTheoryRooms = areaDAO.getActiveTheoryRooms();

        // 3. Handle pipeline action triggers
        String action = request.getParameter("action");
        String regIdStr = request.getParameter("id");
        
        if (action != null) {
            try {
                if ("autoAllocate".equals(action)) {
                    // UC-02 Normal Flow 2.0: Auto allocation parameters
                    int maxCandidatesPerRoom = 30;
                    try {
                        maxCandidatesPerRoom = Integer.parseInt(request.getParameter("maxCandidatesPerRoom"));
                    } catch (Exception e) {}
                    
                    // Filter candidates who need theory room allocation (procedure complete, theory not passed)
                    List<ExamRegistration> readyCandidates = new ArrayList<>();
                    for (ExamRegistration c : qList) {
                        boolean procedureDone = c.isPresent() && (c.getPhotoUrl() != null && !c.getPhotoUrl().isEmpty()) && c.isPaymentCompleted();
                        if (procedureDone && "none".equals(c.getTheoryPassed())) {
                            readyCandidates.add(c);
                        }
                    }

                    int totalCandidates = readyCandidates.size();

                    if (totalCandidates > 0) {
                        // Exception 2.0.E1 check: Seat capacity validation (without expected shifts)
                        int totalSeats = activeTheoryRooms.size() * maxCandidatesPerRoom;
                        if (totalCandidates > totalSeats) {
                            request.setAttribute("errorMsg", "[LỖI Exception 2.0.E1] Vượt quá dung lượng cơ sở hạ tầng. Vui lòng kích hoạt thêm phòng thi lý thuyết.");
                        } else {
                            // Business Rule BR-11 check: 5% backup computer terminals
                            // We get total active computers in all active theory rooms
                            int activeComputersCount = 0;
                            for (ExamArea room : activeTheoryRooms) {
                                activeComputersCount += room.getCapacity(); // Capacity matches total computers
                            }
                            if (activeComputersCount < totalCandidates * 1.05) {
                                request.setAttribute("warningMsg", "[CẢNH BÁO BR-11] Số máy tính thi khả dụng không đủ điều kiện dự phòng 5% cho lượng thí sinh trong ca thi (Quy tắc BR-11)!");
                            }

                            // Prioritize candidates applying for the same license class to group together (BR-10)
                            Collections.sort(readyCandidates, new Comparator<ExamRegistration>() {
                                @Override
                                public int compare(ExamRegistration o1, ExamRegistration o2) {
                                    return o1.getLicenseCode().compareTo(o2.getLicenseCode());
                                }
                            });

                            // Pre-load device pools by category
                            List<ExamDevice> motorbikeDevices = deviceDAO.getAvailableDevicesByCategory("motorbike");
                            List<ExamDevice> carDevices       = deviceDAO.getAvailableDevicesByCategory("car");
                            int motoIdx = 0;
                            int carIdx  = 0;

                            // Pre-load PC list per room ONCE (trước khi gán — tránh query lại sau khi đã InUse)
                            Map<Integer, List<ExamComputer>> roomPCMap = new java.util.LinkedHashMap<>();
                            Map<Integer, Integer>            roomPCIdx = new java.util.HashMap<>();
                            for (ExamArea room : activeTheoryRooms) {
                                List<ExamComputer> pcs = compDAO.getAvailableComputersByArea(room.getId());
                                roomPCMap.put(room.getId(), pcs);
                                roomPCIdx.put(room.getId(), 0);
                            }

                            // Phân bổ tuần tự: từng phòng nhận tối đa maxCandidatesPerRoom thí sinh
                            int candIdx = 0;
                            outer:
                            for (ExamArea room : activeTheoryRooms) {
                                int roomAssigned = 0;
                                List<ExamComputer> roomPCs = roomPCMap.get(room.getId());
                                int pcIdx = roomPCIdx.get(room.getId());

                                while (candIdx < totalCandidates && roomAssigned < maxCandidatesPerRoom) {
                                    ExamRegistration c = readyCandidates.get(candIdx);
                                    c.setAllocatedAreaId(room.getId());
                                    c.setAllocatedAreaName(room.getAreaName());

                                    // Release old computer
                                    if (c.getComputerCode() != null && !c.getComputerCode().isEmpty()) {
                                        String sqlRelease = "update ExamComputer set status = 'Available' where computerCode = ?";
                                        try (Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB;trustServerCertificate=true", "sa", "123");
                                             PreparedStatement ps = conn.prepareStatement(sqlRelease)) {
                                            ps.setString(1, c.getComputerCode());
                                            ps.executeUpdate();
                                        }
                                    }

                                    // Assign PC theo thứ tự từ list đã load sẵn
                                    String newPC = "";
                                    if (pcIdx < roomPCs.size()) {
                                        ExamComputer pc = roomPCs.get(pcIdx);
                                        newPC = pc.getComputerCode();
                                        compDAO.updateStatus(pc.getId(), "InUse");
                                        regDAO.updateComputer(c.getId(), newPC);
                                        pcIdx++;
                                    }
                                    c.setComputerCode(newPC);

                                    // Assign device by license category (round-robin)
                                    String licCode = c.getLicenseCode() != null ? c.getLicenseCode().toUpperCase() : "";
                                    boolean isMotorbike = licCode.startsWith("A");
                                    List<ExamDevice> pool = isMotorbike ? motorbikeDevices : carDevices;
                                    if (!pool.isEmpty()) {
                                        int devIdx = isMotorbike ? (motoIdx % pool.size()) : (carIdx % pool.size());
                                        ExamDevice dev = pool.get(devIdx);
                                        regDAO.updateDevice(c.getId(), dev.getDeviceName());
                                        c.setDeviceCode(dev.getDeviceName());
                                        if (isMotorbike) motoIdx++; else carIdx++;
                                    }

                                    roomAssigned++;
                                    candIdx++;
                                }

                                if (candIdx >= totalCandidates) break outer;
                            }

                            request.setAttribute("alertMsg", "Tự động phân bổ thành công " + totalCandidates + " thí sinh vào phòng máy và thiết bị thi!");
                            addAuditLog(session, "ALLOCATE Candidates", "Tự động phân bổ " + totalCandidates + " thí sinh vào phòng/thiết bị thi.");
                            
                            // Reload từ DB để session khớp DB
                            qList = regDAO.getCandidatesBySession(sessionId);
                            session.setAttribute("candidateQueue", qList);
                            session.setAttribute("lastLoadedSessionId", sessionId);
                        }
                    } else {
                        request.setAttribute("warningMsg", "Không có thí sinh nào đã hoàn thành thủ tục hồ sơ ở phòng chờ cần phân phòng!");
                    }

                } else if ("saveAllocation".equals(action)) {
                    // UC-02 Normal Flow Step 6/7: Save allocation result & activate readiness
                    boolean ok = sessionDAO.updateStatus(sessionId, "InProgress");
                    if (ok) {
                        if (currentSession != null) {
                            currentSession.setStatus("InProgress");
                        }
                        request.setAttribute("alertMsg", "Đã lưu kết quả phân bổ và Kích hoạt trạng thái sẵn sàng thi cho Ca sát hạch thành công!");
                        addAuditLog(session, "ACTIVATE Session", "Kích hoạt ca thi ID: " + sessionId + " sang trạng thái InProgress.");
                    } else {
                        request.setAttribute("errorMsg", "Không thể cập nhật trạng thái hoạt động ca thi trong cơ sở dữ liệu.");
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
                        } else if ("allocatePC".equals(action)) {
                            String compCode = request.getParameter("computerCode");
                            
                            // Release old computer in DB
                            if (profile.getComputerCode() != null && !profile.getComputerCode().isEmpty() && !profile.getComputerCode().equals(compCode)) {
                                String oldCode = profile.getComputerCode();
                                String sqlRelease = "update ExamComputer set status = 'Available' where computerCode = ?";
                                try (Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB;trustServerCertificate=true", "sa", "123");
                                     PreparedStatement ps = conn.prepareStatement(sqlRelease)) {
                                    ps.setString(1, oldCode);
                                    ps.executeUpdate();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            boolean ok = regDAO.updateComputer(regId, compCode);
                            if (ok) {
                                profile.setComputerCode(compCode);
                                // Set new computer as InUse in DB
                                if (compCode != null && !compCode.isEmpty()) {
                                    String sqlInUse = "update ExamComputer set status = 'InUse' where computerCode = ?";
                                    try (Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB;trustServerCertificate=true", "sa", "123");
                                         PreparedStatement ps = conn.prepareStatement(sqlInUse)) {
                                        ps.setString(1, compCode);
                                        ps.executeUpdate();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                addAuditLog(session, "UPDATE ExamRegistration", "Đổi máy thi lý thuyết → " + compCode + " cho SBD " + profile.getSbd());
                            }
                        } else if ("allocateRoom".equals(action)) {
                            // Manual adjustment override (Alternative Flow 2.1)
                            int areaId = Integer.parseInt(request.getParameter("areaId"));
                            ExamArea targetArea = areaDAO.getById(areaId);
                            if (targetArea != null) {
                                // Release old computer in DB
                                if (profile.getComputerCode() != null && !profile.getComputerCode().isEmpty()) {
                                    String oldCode = profile.getComputerCode();
                                    String sqlRelease = "update ExamComputer set status = 'Available' where computerCode = ?";
                                    try (Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB;trustServerCertificate=true", "sa", "123");
                                         PreparedStatement ps = conn.prepareStatement(sqlRelease)) {
                                        ps.setString(1, oldCode);
                                        ps.executeUpdate();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                
                                // Set transient room values
                                profile.setAllocatedAreaId(targetArea.getId());
                                profile.setAllocatedAreaName(targetArea.getAreaName());
                                
                                // Automatically assign a free PC from the new room
                                List<ExamComputer> roomPCs = compDAO.getAvailableComputersByArea(targetArea.getId());
                                String newPC = "";
                                if (!roomPCs.isEmpty()) {
                                    newPC = roomPCs.get(0).getComputerCode();
                                    compDAO.updateStatus(roomPCs.get(0).getId(), "InUse");
                                    regDAO.updateComputer(regId, newPC);
                                } else {
                                    regDAO.updateComputer(regId, null);
                                }
                                profile.setComputerCode(newPC);
                                addAuditLog(session, "UPDATE ExamRegistration", "Chuyển phòng thi → " + targetArea.getAreaName() + " (máy " + newPC + ") cho SBD " + profile.getSbd());
                            }

                        } else if ("allocateDevice".equals(action)) {
                            String devCode = request.getParameter("deviceCode");
                            boolean ok = regDAO.updateDevice(regId, devCode);
                            if (ok) {
                                profile.setDeviceCode(devCode);
                                addAuditLog(session, "UPDATE ExamRegistration", "Đổi thiết bị thi sa hình → " + devCode + " cho SBD " + profile.getSbd());
                            }
                        } else if ("submitTheoryScore".equals(action)) {
                            int score = Integer.parseInt(request.getParameter("score"));
                            String passed = score >= 80 ? "passed" : "failed";
                            boolean ok = regDAO.updateScores(regId, score, passed, null, null);
                            if (ok) {
                                profile.setTheoryScore(score);
                                profile.setTheoryPassed(passed);
                                addAuditLog(session, "UPDATE ExamRegistration", "Nhập điểm LÝ THUYẾT: " + score + " → " + passed.toUpperCase() + " cho SBD " + profile.getSbd());
                                
                                // Release computer back to Available when theory is completed!
                                if (profile.getComputerCode() != null && !profile.getComputerCode().isEmpty()) {
                                    String oldCode = profile.getComputerCode();
                                    String sqlRelease = "update ExamComputer set status = 'Available' where computerCode = ?";
                                    try (Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB;trustServerCertificate=true", "sa", "123");
                                         PreparedStatement ps = conn.prepareStatement(sqlRelease)) {
                                        ps.setString(1, oldCode);
                                        ps.executeUpdate();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else if ("submitPracticalScore".equals(action)) {
                            int score = Integer.parseInt(request.getParameter("score"));
                            String passed = score >= 80 ? "passed" : "failed";
                            boolean ok = regDAO.updateScores(regId, null, null, score, passed);
                            if (ok) {
                                profile.setPracticalScore(score);
                                profile.setPracticalPassed(passed);
                                addAuditLog(session, "UPDATE ExamRegistration", "Nhập điểm SA HÌNH: " + score + " → " + passed.toUpperCase() + " cho SBD " + profile.getSbd());
                            }
                        } else if ("submitRoadScore".equals(action)) {
                            int score = Integer.parseInt(request.getParameter("score"));
                            String passed = score >= 80 ? "passed" : "failed";
                            boolean ok = regDAO.updateRoadScore(regId, score, passed);
                            if (ok) {
                                profile.setRoadTestScore(score);
                                profile.setRoadTestPassed(passed);
                                addAuditLog(session, "UPDATE ExamRegistration", "Nhập điểm ĐƯỜNG TRƯỜNG: " + score + " → " + passed.toUpperCase() + " cho SBD " + profile.getSbd());
                            }
                        } else if ("quickComplete".equals(action)) {
                            // Simulates complete desk registrations (photo + payment)
                            String photoPath = "assets/imgs/candidates/" + profile.getSbd() + "_captured.png";
                            regDAO.updatePhoto(regId, photoPath);
                            regDAO.updatePayment(regId, true);
                            
                            profile.setPhotoUrl(photoPath);
                            profile.setIsPaymentCompleted(true);
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
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedSessionId", sessionId);

        request.setAttribute("activeTheoryRooms", areaDAO.getActiveTheoryRooms());
        request.setAttribute("availableComputers", compDAO.getAvailableComputers());
        request.setAttribute("availableDevices", deviceDAO.getAvailableDevices(null));
        request.setAttribute("motorbikeDevices", deviceDAO.getAvailableDevicesByCategory("motorbike"));
        request.setAttribute("carDevices",       deviceDAO.getAvailableDevicesByCategory("car"));

        // Resolve allocated room name for display only (no PC re-assignment)
        for (ExamRegistration c : qList) {
            if (c.getAllocatedAreaId() == null && c.getComputerCode() != null && !c.getComputerCode().isEmpty()) {
                ExamArea area = areaDAO.getAreaByComputerCode(c.getComputerCode());
                if (area != null) {
                    c.setAllocatedAreaId(area.getId());
                    c.setAllocatedAreaName(area.getAreaName());
                }
            }
        }

        request.getRequestDispatcher("/views/staff/examstaff/allocation.jsp").forward(request, response);
    }

    private void addAuditLog(HttpSession session, String action, String details) {
        try {
            Models.User user = (Models.User) session.getAttribute("user");
            int userId = (user != null && user.getId() > 0) ? user.getId() : 3; // Default staff
            
            // Chuẩn hóa action sang giá trị hợp lệ cho CHECK constraint DB
            String upper = action.toUpperCase();
            String dbAction;
            if (upper.contains("INSERT") || upper.contains("IMPORT")) dbAction = "INSERT";
            else if (upper.contains("DELETE"))                         dbAction = "DELETE";
            else if (upper.contains("EXPORT"))                         dbAction = "EXPORT";
            else                                                        dbAction = "UPDATE";
            
            // Xác định tableName từ action gốc
            String tableName;
            if (upper.contains("PAYMENT"))    tableName = "Payment";
            else if (upper.contains("PERSON")) tableName = "Person";
            else                               tableName = "ExamRegistration";
            
            // recordId NOT NULL — dùng 0 nếu không có entity cụ thể
            String sql = "insert into AuditLog (tableName, recordId, action, newValue, changedBy, changedAt) values (?, ?, ?, ?, ?, ?)";
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB;trustServerCertificate=true", "sa", "123");
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tableName);
                ps.setInt(2, 0);
                ps.setString(3, dbAction);
                ps.setString(4, action + " | " + details);
                ps.setInt(5, userId);
                ps.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();
            }
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
