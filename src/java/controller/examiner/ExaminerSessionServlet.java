package controller.examiner;

import enums.ExamSection;
import enums.ExamSessionStatus;
import filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
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
import service.ExamSessionService;
import service.ExaminerService;
import service.LicenceService;
import service.impl.ExamAreaServiceImpl;
import service.impl.ExamServiceImpl;
import service.impl.ExamSessionServiceImpl;
import service.impl.ExaminerServiceImpl;
import service.impl.LicenceServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/views/examiner/session")
public class ExaminerSessionServlet extends BaseExaminerServlet {

    private final ExaminerService examinerService = new ExaminerServiceImpl();
    private final ExamSessionService examSessionService = new ExamSessionServiceImpl();
    private final ExamService examService = new ExamServiceImpl();
    private final ExamAreaService examAreaService = new ExamAreaServiceImpl();
    private final LicenceService licenceService = new LicenceServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession httpSession = requireSession(request, response);
        if (httpSession == null) {
            return;
        }
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }

        List<ExaminerSchedule> schedules = examinerService.getSchedulesByExaminerId(user.getUserId());
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
        HttpSession httpSession = requireSession(request, response);
        if (httpSession == null) {
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

        ExaminerSchedule schedule = examinerService.getScheduleById(scheduleId);
        if (schedule == null || schedule.getExaminerId() != user.getUserId()) {
            response.sendRedirect(request.getContextPath() + "/views/examiner/session?error=denied");
            return;
        }

        Session session = examSessionService.getById(schedule.getSessionId());
        if (session == null || ExamSessionStatus.fromValue(session.getStatus()) != ExamSessionStatus.IN_PROGRESS) {
            response.sendRedirect(request.getContextPath() + "/views/examiner/session?error=notActive");
            return;
        }

        schedule = hydrateSchedule(schedule, new HashMap<>());
        ExamSection examSection = examSessionService.resolveExamSection(schedule, session);
        applyExaminerSessionContext(httpSession, schedule, session, examSection);
        response.sendRedirect(request.getContextPath() + "/views/examiner/dashboard");
    }

    private ExaminerSchedule hydrateSchedule(ExaminerSchedule schedule, Map<Integer, Licence> licencesByExamId) {
        Session session = examSessionService.getById(schedule.getSessionId());
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
        schedule.setExamSection(examSessionService.getExamSectionModel(schedule, session));
        return schedule;
    }
}
