package Controllers.Staff.ManagingStaff;

import DAOs.ExamAreaDAO;
import DAOs.ExamSessionDAO;
import DAOs.LicenceDAO;
import DAOs.Impl.ExamAreaDAOImpl;
import DAOs.Impl.ExamSessionDAOImpl;
import DAOs.Impl.LicenceDAOImpl;
import DBConnection.DBContext;
import DTOs.SessionDTO;
import Models.ExamArea;
import Models.Licence;
import Models.User;
import Utils.AuditLogHelper;
import Utils.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

@WebServlet("/manager/exam-schedules")
public class ExamScheduleServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/exam-schedules.jsp";
    private static final Set<String> ALLOWED_STATUS = Set.of("Scheduled", "Open", "Closed", "Cancelled");

    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
    private final LicenceDAO licenceDAO = new LicenceDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }
        bindPageData(request);
        moveFlash(request, "scheduleSuccess");
        moveFlash(request, "scheduleError");
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }

        String action = trim(request.getParameter("action"));
        if ("status".equalsIgnoreCase(action)) {
            updateStatus(request, response);
            return;
        }

        createSession(request, response);
    }

    private void createSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String sessionName = trim(request.getParameter("sessionName"));
        String centreName = trim(request.getParameter("centreName"));
        int licenceId = parseInt(request.getParameter("licenceId"), 0);
        int areaId = parseInt(request.getParameter("areaId"), 0);
        int sectionId = parseInt(request.getParameter("sectionId"), 0);
        String examDate = trim(request.getParameter("examDate"));
        String start = trim(request.getParameter("startTime"));
        String end = trim(request.getParameter("endTime"));

        String error = validateCreate(sessionName, centreName, licenceId, areaId, sectionId, examDate, start, end);
        if (error != null) {
            request.setAttribute("scheduleError", error);
            bindPageData(request);
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }

        try {
            Timestamp startTime = toTimestamp(examDate, start);
            Timestamp endTime = toTimestamp(examDate, end);
            int sessionId = sessionDAO.createManagedSession(
                    sessionName, licenceId, areaId, sectionId, startTime, endTime, centreName);

            if (sessionId <= 0) {
                request.getSession().setAttribute("scheduleError", "Không tạo được phiên thi. Vui lòng kiểm tra dữ liệu hoặc log server.");
            } else {
                request.getSession().setAttribute("scheduleSuccess", "Đã tạo phiên thi mới: " + sessionName);
                AuditLogHelper.persist(request.getSession(), "INSERT SESSION", "Tạo phiên thi: " + sessionName, sessionId);
            }
            response.sendRedirect(request.getContextPath() + "/manager/exam-schedules");
        } catch (IllegalArgumentException ex) {
            request.setAttribute("scheduleError", ex.getMessage());
            bindPageData(request);
            request.getRequestDispatcher(VIEW).forward(request, response);
        }
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int sessionId = parseInt(request.getParameter("sessionId"), 0);
        String status = trim(request.getParameter("status"));
        if (sessionId <= 0 || !ALLOWED_STATUS.contains(status)) {
            request.getSession().setAttribute("scheduleError", "Trạng thái phiên thi không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/manager/exam-schedules");
            return;
        }

        boolean ok = sessionDAO.updateStatus(sessionId, status);
        request.getSession().setAttribute(ok ? "scheduleSuccess" : "scheduleError",
                ok ? "Đã cập nhật trạng thái phiên thi." : "Không cập nhật được trạng thái phiên thi.");
        if (ok) {
            AuditLogHelper.persist(request.getSession(), "UPDATE SESSION",
                    "Cập nhật trạng thái phiên thi thành " + status, sessionId);
        }
        response.sendRedirect(request.getContextPath() + "/manager/exam-schedules");
    }

    private String validateCreate(String sessionName, String centreName, int licenceId, int areaId, int sectionId,
                                  String examDate, String start, String end) {
        if (sessionName.length() < 3 || sessionName.length() > 100) {
            return "Tên phiên thi phải từ 3 đến 100 ký tự.";
        }
        if (centreName.length() < 3 || centreName.length() > 255) {
            return "Tên trung tâm/địa điểm thi phải từ 3 đến 255 ký tự.";
        }
        if (licenceId <= 0 || areaId <= 0 || sectionId <= 0) {
            return "Vui lòng chọn hạng GPLX, khu vực thi và phần thi.";
        }
        try {
            Timestamp startTime = toTimestamp(examDate, start);
            Timestamp endTime = toTimestamp(examDate, end);
            if (!endTime.after(startTime)) {
                return "Giờ kết thúc phải sau giờ bắt đầu.";
            }
            if (startTime.toLocalDateTime().toLocalDate().isBefore(LocalDate.now())) {
                return "Không thể tạo phiên thi trong quá khứ.";
            }
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
        return null;
    }

    private void bindPageData(HttpServletRequest request) {
        List<SessionDTO> sessions = sessionDAO.getAllSessions();
        List<Licence> licences = licenceDAO.findAll();
        List<ExamArea> areas = areaDAO.search(null, null);
        List<ExamSectionOption> sections = findExamSections();

        request.setAttribute("sessions", sessions);
        request.setAttribute("licences", licences);
        request.setAttribute("areas", areas);
        request.setAttribute("sections", sections);
        request.setAttribute("today", LocalDate.now().toString());
    }

    private List<ExamSectionOption> findExamSections() {
        String sql = "SELECT ExamSectionId, SectionName FROM ExamSection ORDER BY ExamSectionId";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            java.util.ArrayList<ExamSectionOption> list = new java.util.ArrayList<>();
            while (rs.next()) {
                list.add(new ExamSectionOption(rs.getInt("ExamSectionId"), rs.getString("SectionName")));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private Timestamp toTimestamp(String date, String time) {
        try {
            LocalDate d = LocalDate.parse(date);
            LocalTime t = LocalTime.parse(time);
            return Timestamp.valueOf(LocalDateTime.of(d, t));
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("Ngày thi hoặc giờ thi không hợp lệ.");
        }
    }

    private boolean hasAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        String role = user.getRole() == null ? "" : user.getRole().getRoleName();
        if (!"ManagingStaff".equalsIgnoreCase(role) && !"Admin".equalsIgnoreCase(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private void moveFlash(HttpServletRequest request, String name) {
        HttpSession session = request.getSession();
        Object value = session.getAttribute(name);
        if (value != null) {
            request.setAttribute(name, value);
            session.removeAttribute(name);
        }
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(trim(raw));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String trim(String raw) {
        return raw == null ? "" : raw.trim();
    }

    public static class ExamSectionOption {
        private final int id;
        private final String name;

        public ExamSectionOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
