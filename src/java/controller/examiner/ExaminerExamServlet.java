package controller.examiner;

import enums.SectionType;
import enums.ExamStatus;
import filter.ExaminerFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Exam;
import model.ExamArea;
import model.ExaminerSchedule;
import model.ExamSection;
import model.Licence;
import model.User;
import service.ExamAreaService;
import service.ExamService;
import service.ExamSectionService;
import service.ScheduleService;
import service.LicenceService;
import service.impl.ExamAreaServiceImpl;
import service.impl.ExamServiceImpl;
import service.impl.ExamSectionServiceImpl;
import service.impl.ScheduleServiceImpl;
import service.impl.LicenceServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/old_views/examiner/exam")
public class ExaminerExamServlet extends HttpServlet {

    private final ScheduleService scheduleService = new ScheduleServiceImpl();
    private final ExamService examService = new ExamServiceImpl();
    private final ExamAreaService examAreaService = new ExamAreaServiceImpl();
    private final ExamSectionService examSectionService = new ExamSectionServiceImpl();
    private final LicenceService licenceService = new LicenceServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) { response.sendRedirect(request.getContextPath() + "/staff/login"); return; }
        User user = (User) httpSession.getAttribute("user");
        if (user == null) { response.sendRedirect(request.getContextPath() + "/staff/login"); return; }

        List<ExaminerSchedule> schedules = scheduleService.getSchedulesByExaminerId(user.getUserId());
        Map<Integer, Licence> licencesByExamId = new HashMap<>();
        List<ExaminerSchedule> hydrated = new ArrayList<>();
        for (ExaminerSchedule schedule : schedules) {
            hydrated.add(hydrateSchedule(schedule, licencesByExamId));
        }
        request.setAttribute("schedules", hydrated);
        request.setAttribute("licencesByExamId", licencesByExamId);
        request.getRequestDispatcher("/old_views/examiner/exam-select.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) { response.sendRedirect(request.getContextPath() + "/staff/login"); return; }
        User user = (User) httpSession.getAttribute("user");
        if (user == null) { response.sendRedirect(request.getContextPath() + "/staff/login"); return; }

        int examId;
        try {
            examId = Integer.parseInt(request.getParameter("examId"));
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/old_views/examiner/exam?error=invalid");
            return;
        }

        ExaminerSchedule schedule = scheduleService.getScheduleByExaminerAndExam(user.getUserId(), examId);
        if (schedule == null) {
            response.sendRedirect(request.getContextPath() + "/old_views/examiner/exam?error=denied");
            return;
        }

        Exam exam = examService.getById(examId);
        if (exam == null || ExamStatus.fromValue(exam.getStatus()) != ExamStatus.IN_PROGRESS) {
            response.sendRedirect(request.getContextPath() + "/old_views/examiner/exam?error=notActive");
            return;
        }

        schedule = hydrateSchedule(schedule, new HashMap<>());
        SectionType examSection = (schedule.getExamSectionId() != null && schedule.getExamSectionId() > 0)
                ? SectionType.fromValue(examSectionService.getById(schedule.getExamSectionId()).getSectionType())
                : null;

        httpSession.setAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE, schedule);
        httpSession.setAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID, exam.getExamId());
        httpSession.setAttribute("isTheory", examSection == SectionType.THEORY);
        httpSession.setAttribute(ExaminerFilter.ATTR_EXAM_SECTION, examSection);
        httpSession.setAttribute("examSectionName", examSection != null ? examSection.getValue() : null);

        response.sendRedirect(request.getContextPath() + "/old_views/examiner/dashboard");
    }

    private ExaminerSchedule hydrateSchedule(ExaminerSchedule schedule, Map<Integer, Licence> licencesByExamId) {
        Exam exam = examService.getById(schedule.getExamId());
        schedule.setExam(exam);
        if (exam != null && exam.getLicenceId() > 0 && !licencesByExamId.containsKey(exam.getExamId())) {
            Licence licence = licenceService.getById(exam.getLicenceId());
            if (licence != null) licencesByExamId.put(exam.getExamId(), licence);
        }
        if (schedule.getExamSectionId() != null && schedule.getExamSectionId() > 0) {
            schedule.setExamSection(examSectionService.getById(schedule.getExamSectionId()));
        }
        if (schedule.getExamAreaId() != null && schedule.getExamAreaId() > 0) {
            ExamArea area = examAreaService.getById(schedule.getExamAreaId());
            schedule.setExamArea(area);
        }
        return schedule;
    }
}
