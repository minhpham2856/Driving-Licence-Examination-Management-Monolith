package examiner.controller;

import shared.enums.SectionType;
import shared.enums.ExamStatus;
import examiner.filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.model.Exam;
import shared.model.ExamArea;
import shared.model.ExaminerSchedule;
import shared.model.Licence;
import auth.dto.UserDTO;
import shared.Attributes;
import examiner.service.ExamService;
import examiner.service.impl.ExamServiceImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/examiner/exam")
// Exam session selection: lists assigned schedules on GET and binds the chosen in-progress exam into session on POST.
public class ExamServlet extends HttpServlet {

    private final ExamService examService = new ExamServiceImpl();

    // List hydrated examiner schedules with licence info and forward to the session selection page.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }
        // Staff login stores UserDTO on session; examiner pages reuse staff auth.
        UserDTO user = (UserDTO) httpSession.getAttribute(Attributes.Session.USER);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }

        // Load all schedules assigned to this examiner and enrich each row for exam-select.jsp.
        List<ExaminerSchedule> schedules = examService.getAllByExaminer(user.getUserId());
        Map<Integer, Licence> licencesByExamId = new HashMap<>();
        List<ExaminerSchedule> hydrated = new ArrayList<>();
        for (ExaminerSchedule schedule : schedules) {
            hydrated.add(hydrateSchedule(schedule, licencesByExamId));
        }
        request.setAttribute("schedules", hydrated);
        request.setAttribute("licencesByExamId", licencesByExamId);
        request.getRequestDispatcher("/views/examiner/exam-select.jsp").forward(request, response);
    }

    // Validate the selected exam is in progress, store schedule context in session, and redirect to the dashboard.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }
        UserDTO user = (UserDTO) httpSession.getAttribute(Attributes.Session.USER);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }

        int examId;
        try {
            examId = Integer.parseInt(request.getParameter("examId"));
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/examiner/exam?error=invalid");
            return;
        }

        // Examiner must be scheduled for the chosen exam.
        ExaminerSchedule schedule = examService.getIfByExaminerAndExam(user.getUserId(), examId);
        if (schedule == null) {
            response.sendRedirect(request.getContextPath() + "/examiner/exam?error=denied");
            return;
        }

        Exam exam = examService.get(examId);
        if (exam == null) {
            response.sendRedirect(request.getContextPath() + "/examiner/exam?error=notActive");
            return;
        }
        ExamStatus examStatus = ExamStatus.fromValue(exam.getStatus());
        if (examStatus == ExamStatus.PAUSED) {
            response.sendRedirect(request.getContextPath() + "/examiner/exam?error=paused");
            return;
        }
        if (examStatus != ExamStatus.IN_PROGRESS) {
            response.sendRedirect(request.getContextPath() + "/examiner/exam?error=notActive");
            return;
        }

        // Hydrate schedule and resolve section enum for ExaminerFilter session keys.
        schedule = hydrateSchedule(schedule, new HashMap<>());
        SectionType sectionType = (schedule.getExamSectionId() != null && schedule.getExamSectionId() > 0)
                ? SectionType.fromValue(examService.getBySectionId(schedule.getExamSectionId()).getSectionType())
                : null;

        // Bind active exam context used by all subsequent examiner pages.
        httpSession.setAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE, schedule);
        httpSession.setAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID, exam.getExamId());
        httpSession.setAttribute("isTheory", sectionType == SectionType.THEORY);
        httpSession.setAttribute(ExaminerFilter.ATTR_EXAM_SECTION, sectionType);

        response.sendRedirect(request.getContextPath() + "/examiner/dashboard");
    }

    // Attach exam, section, area, and licence references to a schedule row for display.
    private ExaminerSchedule hydrateSchedule(ExaminerSchedule schedule, Map<Integer, Licence> licencesByExamId) {
        Exam exam = examService.get(schedule.getExamId());
        schedule.setExam(exam);
        if (exam != null && exam.getLicenceId() > 0 && !licencesByExamId.containsKey(exam.getExamId())) {
            Licence licence = examService.getByLicenceId(exam.getLicenceId());
            if (licence != null) {
                licencesByExamId.put(exam.getExamId(), licence);
            }
        }
        if (schedule.getExamSectionId() != null && schedule.getExamSectionId() > 0) {
            schedule.setExamSection(examService.getBySectionId(schedule.getExamSectionId()));
        }
        if (schedule.getExamAreaId() != null && schedule.getExamAreaId() > 0) {
            ExamArea area = examService.getByAreaId(schedule.getExamAreaId());
            schedule.setExamArea(area);
        }
        return schedule;
    }
}
