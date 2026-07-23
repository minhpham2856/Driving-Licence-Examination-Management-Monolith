package managingstaff.controller;

import managingstaff.dao.ExamSessionDAO;
import managingstaff.dao.impl.ExamSessionDAOImpl;
import managingstaff.dto.SessionDTO;
import auth.dto.UserDTO;
import managingstaff.util.AuditLogHelper;
import managingstaff.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import managingstaff.dao.impl.LicenceDAOImpl;
import managingstaff.dao.impl.TentativeExamDateDAOImpl;
import shared.model.Licence;
import managingstaff.service.EmailService;
import managingstaff.service.impl.EmailServiceImpl;
import managingstaff.dto.ExamRegistrationDTO;
import java.time.format.DateTimeFormatter;

@WebServlet("/manager/exam-schedules")
public class ExamScheduleServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/exam-schedules.jsp";
    private static final Set<String> ALLOWED_STATUS = Set.of(
            "Chưa diễn ra", "Đang diễn ra", "Hoàn tất", "Đã hủy");

    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
    private static final int PAGE_SIZE = 10;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }
        int editId = parseInt(request.getParameter("edit"), 0);
        int viewId = parseInt(request.getParameter("view"), 0);
        if (editId > 0) {
            SessionDTO editing = sessionDAO.findById(editId);
            if (editing == null || !editing.isEditable()) {
                request.getSession().setAttribute("scheduleError", "Chỉ được sửa phiên thi chưa diễn ra.");
                response.sendRedirect(request.getContextPath() + "/manager/exam-schedules");
                return;
            }
            request.setAttribute("editingSession", editing);
        }
        if (viewId > 0) {
            request.setAttribute("viewSession", sessionDAO.findById(viewId));
            request.setAttribute("sessionCandidates", sessionDAO.getCandidates(viewId));
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
        if ("save".equalsIgnoreCase(action)) saveSession(request, response);
        else if ("cancel".equalsIgnoreCase(action)) cancelSession(request, response);
        else response.sendRedirect(request.getContextPath() + "/manager/exam-schedules");
    }

    private void cancelSession(HttpServletRequest request,HttpServletResponse response)throws IOException{
        int id=parseInt(request.getParameter("sessionId"),0);String reason=trim(request.getParameter("reason"));
        SessionDTO exam=sessionDAO.findById(id);
        if(exam==null||!exam.isEditable()||reason.length()<5){request.getSession().setAttribute("scheduleError","Chỉ được hủy phiên chưa thi và phải nhập lý do.");}
        else if(sessionDAO.cancel(id)){int sent=sendCancellationEmails(exam,reason);request.getSession().setAttribute("scheduleSuccess","Đã hủy phiên thi; danh sách vẫn được giữ lại. Đã gửi "+sent+" email.");AuditLogHelper.persist(request.getSession(),"CANCEL SESSION","Hủy phiên "+exam.getSessionName()+". Lý do: "+reason,id);}
        else request.getSession().setAttribute("scheduleError","Không thể hủy phiên thi.");
        response.sendRedirect(request.getContextPath()+"/manager/exam-schedules?tab=cancelled");
    }

    private int sendCancellationEmails(SessionDTO exam,String reason){EmailService mail=new EmailServiceImpl();if(!mail.isConfigured())return 0;int sent=0;for(ExamRegistrationDTO c:sessionDAO.getCandidates(exam.getId())){String body="Xin chào "+c.getFullName()+",\n\nPhiên sát hạch "+exam.getSessionName()+" ngày "+exam.getExamDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))+" đã hủy.\nLý do: "+reason+".\nHồ sơ và danh sách của bạn vẫn được lưu; đơn vị tổ chức sẽ thông báo lịch mới.";if(mail.sendTextEmail(c.getEmail(),"Thông báo hủy lịch sát hạch",body))sent++;}return sent;}

    private void saveSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession httpSession = request.getSession();
        try {
            SessionDTO dto = new SessionDTO();
            dto.setId(parseInt(request.getParameter("sessionId"), 0));
            dto.setCentreName(trim(request.getParameter("centreName")));
            dto.setLicenceId(parseInt(request.getParameter("licenceId"), 0));
            dto.setSourceExamDateId(parseInt(request.getParameter("sourceExamDateId"), 0));
            LocalDate date = LocalDate.parse(trim(request.getParameter("examDate")));
            dto.setExamDate(Date.valueOf(date));
            dto.setShiftStartTime(Time.valueOf(trim(request.getParameter("startTime")) + ":00"));
            Licence licence = new LicenceDAOImpl().findById(dto.getLicenceId());
            if (licence == null || dto.getCentreName().length() < 3)
                throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin phiên thi.");
            dto.setLicenseCode(licence.getLicenceClass());
            SessionDTO previous=dto.getId()>0?sessionDAO.findById(dto.getId()):null;
            if(previous!=null&&previous.getRegisteredCount()>0){dto.setLicenceId(previous.getLicenceId());dto.setLicenseCode(previous.getLicenseCode());licence=new LicenceDAOImpl().findById(dto.getLicenceId());}
            int id = dto.getId() > 0 ? (sessionDAO.update(dto) ? dto.getId() : 0) : sessionDAO.create(dto);
            int rescheduleEmails=previous!=null&&!previous.getExamDate().equals(dto.getExamDate())
                    ? sendRescheduleEmails(previous,dto) : 0;
            int officialCandidateCount = dto.getId() > 0 ? 0 : sessionDAO.getCandidates(id).size();
            httpSession.setAttribute("scheduleSuccess", dto.getId() > 0
                    ? "Đã cập nhật phiên thi"+(rescheduleEmails>0?" và gửi "+rescheduleEmails+" email lịch mới.":".")
                    : "Đã tạo phiên thi và tự động tiếp nhận " + officialCandidateCount
                    + " thí sinh từ danh sách chính thức của CSGT.");
            AuditLogHelper.persist(httpSession, dto.getId() > 0 ? "UPDATE SESSION" : "CREATE SESSION",
                    (dto.getId() > 0 ? "Cập nhật" : "Tạo") + " phiên thi " + licence.getLicenceClass(), id);
        } catch (Exception ex) {
            httpSession.setAttribute("scheduleError", ex.getMessage() == null ? "Dữ liệu phiên thi không hợp lệ." : ex.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/manager/exam-schedules");
    }
    private int sendRescheduleEmails(SessionDTO old,SessionDTO updated){EmailService mail=new EmailServiceImpl();if(!mail.isConfigured())return 0;int sent=0;String oldDate=old.getExamDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),newDate=updated.getExamDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));for(ExamRegistrationDTO c:sessionDAO.getCandidates(old.getId())){String body="Xin chào "+c.getFullName()+",\n\nLịch sát hạch của bạn đã được chuyển từ "+oldDate+" sang "+newDate+" tại "+updated.getCentreName()+".\nSố báo danh của bạn được giữ nguyên: "+c.getSbd()+".";if(mail.sendTextEmail(c.getEmail(),"Thông báo thay đổi lịch sát hạch",body))sent++;}return sent;}

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

    private void bindPageData(HttpServletRequest request) {
        String tab=trim(request.getParameter("tab"));
        if(!Set.of("upcoming","ongoing","completed","cancelled").contains(tab))tab="upcoming";
        List<Integer> years=parseYears(request.getParameterValues("year"));
        int total=sessionDAO.count(tab,years),pages=Math.max(1,(total+PAGE_SIZE-1)/PAGE_SIZE);
        int page=Math.min(Math.max(1,parseInt(request.getParameter("page"),1)),pages);
        List<SessionDTO> sessions=sessionDAO.findPage(tab,years,page,PAGE_SIZE);
        request.setAttribute("sessions", sessions);
        request.setAttribute("licences", new LicenceDAOImpl().findAll());
        request.setAttribute("policeCompletedDates", new TentativeExamDateDAOImpl().findPoliceCompletedUnlinked());
        request.setAttribute("today", LocalDate.now().toString());
        request.setAttribute("activeTab",tab);request.setAttribute("selectedYears",years);
        request.setAttribute("availableYears",sessionDAO.findAvailableYears());
        request.setAttribute("currentPage",page);request.setAttribute("totalPages",pages);request.setAttribute("totalSessions",total);
        request.setAttribute("upcomingCount",sessionDAO.count("upcoming",List.of()));
        request.setAttribute("ongoingCount",sessionDAO.count("ongoing",List.of()));
        request.setAttribute("completedCount",sessionDAO.count("completed",List.of()));
        request.setAttribute("cancelledCount",sessionDAO.count("cancelled",List.of()));
    }

    private List<Integer> parseYears(String[] values){List<Integer> r=new java.util.ArrayList<>();if(values!=null)for(String v:values){int y=parseInt(v,0);if(y>=2000&&y<=2100&&!r.contains(y))r.add(y);}return r;}

    private boolean hasAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UserDTO user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!SessionUtil.isManager(user)) {
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
}
