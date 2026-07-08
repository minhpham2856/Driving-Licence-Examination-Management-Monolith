package controller.staff.exam;

import dto.EnrollmentDTO;
import dto.SessionViewDTO;
import enums.CandidateStatus;
import enums.ExamSessionStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.RegistrationService;
import service.SessionService;
import service.impl.RegistrationServiceImpl;
import service.impl.SessionServiceImpl;
import controller.staff.exam.BaseStaffExamServlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/views/staff/exam/dashboard")
public class StaffExamDashboardServlet extends BaseStaffExamServlet {

    private final SessionService sessionService = new SessionServiceImpl();
    private final RegistrationService regService = new RegistrationServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        int sessionId = readSessionId(request, session, sessionService);
        if (sessionId > 0) {
            session.setAttribute("selectedSessionId", sessionId);
        }
        SessionViewDTO SessionViewDTO = sessionId > 0 ? sessionService.getSessionById(sessionId) : null;
        if (SessionViewDTO != null) {
            request.setAttribute("activeSessionShift", SessionViewDTO.getCaLabel());
            ExamSessionStatus status = ExamSessionStatus.fromValue(SessionViewDTO.getStatus());
            if (status == ExamSessionStatus.IN_PROGRESS) {
                request.setAttribute("activeSessionStatus", "Đang diễn ra");
            } else if (status == ExamSessionStatus.COMPLETED) {
                request.setAttribute("activeSessionStatus", "Đã kết thúc");
            } else {
                request.setAttribute("activeSessionStatus", SessionViewDTO.getStatus());
            }
        }
        List<EnrollmentDTO> candidates = sessionId > 0
                ? regService.getCandidatesBySession(sessionId) : new ArrayList<>();
        int total = candidates.size();
        int active = 0;
        int passed = 0;
        for (EnrollmentDTO c : candidates) {
            CandidateStatus st = CandidateStatus.fromValue(c.getSectionStatus());
            if (st == CandidateStatus.IN_PROGRESS) {
                active++;
            }
            if (st == CandidateStatus.COMPLETED) {
                passed++;
            }
        }
        request.setAttribute("totalExaminees", total);
        request.setAttribute("activeExaminees", active);
        request.setAttribute("passedExaminees", passed);
        request.setAttribute("sessionCandidatesTrend", total + " đăng ký ca này");
        request.setAttribute("successRate", total > 0 ? (passed * 100 / total) + "%" : "0%");
        request.setAttribute("liveComputers", buildLiveComputers(candidates, SessionViewDTO));
        request.getRequestDispatcher("/views/staff/exam/dashboard.jsp").forward(request, response);
    }

    private List<Map<String, Object>> buildLiveComputers(List<EnrollmentDTO> candidates, SessionViewDTO SessionViewDTO) {
        List<Map<String, Object>> computers = new ArrayList<>();
        int index = 1;
        for (EnrollmentDTO c : candidates) {
            if (CandidateStatus.fromValue(c.getSectionStatus()) != CandidateStatus.IN_PROGRESS) {
                continue;
            }
            Map<String, Object> comp = new LinkedHashMap<>();
            comp.put("computerName", "MT-LT-" + String.format("%02d", index++));
            comp.put("candidateName", c.getFullName());
            comp.put("sbd", String.valueOf(c.getSbd()));
            comp.put("licenseClass", SessionViewDTO != null ? SessionViewDTO.getLicenseCode() : "-");
            comp.put("status", "testing");
            comp.put("extraInfo", "--:--");
            computers.add(comp);
            if (computers.size() >= 6) {
                break;
            }
        }
        return computers;
    }
}
