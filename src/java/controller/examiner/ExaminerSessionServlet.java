package controller.examiner;

import enums.SectionType;
import enums.ExamSessionStatus;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Exam;
import model.ExamArea;
import model.ExaminerSchedule;
import model.Licence;
import model.Session;
import model.User;
import service.ExamAreaService;
import service.ExamService;
import service.SessionService;
import service.ScheduleService;
import service.LicenceService;
import service.impl.ExamAreaServiceImpl;
import service.impl.ExamServiceImpl;
import service.impl.SessionServiceImpl;
import service.impl.ScheduleServiceImpl;
import service.impl.LicenceServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/views/examiner/session")
public class ExaminerSessionServlet extends HttpServlet {

    private final ScheduleService ScheduleService = new ScheduleServiceImpl();
    private final SessionService SessionService = new SessionServiceImpl();
    private final ExamService examService = new ExamServiceImpl();
    private final ExamAreaService examAreaService = new ExamAreaServiceImpl();
    private final LicenceService licenceService = new LicenceServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }

        List<ExaminerSchedule> schedules = ScheduleService.getSchedulesByExaminerId(user.getUserId());
        Map<Integer, Licence> licencesByExamId = new HashMap<>();
        List<ExaminerSchedule> hydrated = new ArrayList<>();
        for (ExaminerSchedule schedule : schedules) {
            hydrated.add(hydrateSchedule(schedule, licencesByExamId));
        }

        request.setAttribute("schedules", hydrated);
        request.setAttribute("licencesByExamId", licencesByExamId);
        request.getRequestDispatcher("/views/examiner/session-select.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }

        int scheduleId;
        try {
            scheduleId = Integer.parseInt(request.getParameter("examinerScheduleId"));
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/views/examiner/session?error=invalid");
            return;
        }

        ExaminerSchedule schedule = ScheduleService.getScheduleById(scheduleId);
        if (schedule == null || schedule.getExaminerId() != user.getUserId()) {
            response.sendRedirect(request.getContextPath() + "/views/examiner/session?error=denied");
            return;
        }

        Session session = SessionService.getById(schedule.getSessionId());
        if (session == null || ExamSessionStatus.fromValue(session.getStatus()) != ExamSessionStatus.IN_PROGRESS) {
            response.sendRedirect(request.getContextPath() + "/views/examiner/session?error=notActive");
            return;
        }

        schedule = hydrateSchedule(schedule, new HashMap<>());
        SectionType examSection = SessionService.getExamSection(schedule, session);
        
        httpSession.setAttribute("activeSessionId", schedule.getSessionId());
        httpSession.setAttribute("isTheory", examSection == SectionType.THEORY);
        httpSession.setAttribute("examSectionName", examSection != null ? examSection.getValue() : null);
        
        response.sendRedirect(request.getContextPath() + "/views/examiner/dashboard");
    }

    private ExaminerSchedule hydrateSchedule(ExaminerSchedule schedule, Map<Integer, Licence> licencesByExamId) {
        Session session = SessionService.getById(schedule.getSessionId());
        schedule.setSession(session);
        if (session != null) {
            Exam exam = examService.getById(session.getExamId());
            session.setExam(exam);
            if (exam != null && exam.getLicenceId() > 0 && !licencesByExamId.containsKey(exam.getExamId())) {
                Licence licence = licenceService.getById(exam.getLicenceId());
                if (licence != null) {
                    licencesByExamId.put(exam.getExamId(), licence);
                }
            }
        }
        if (schedule.getExamAreaId() != null && schedule.getExamAreaId() > 0) {
            ExamArea area = examAreaService.getById(schedule.getExamAreaId());
            schedule.setExamArea(area);
        }
        schedule.setExamSection(SessionService.getExamSectionModel(schedule, session));
        return schedule;
    }
}
