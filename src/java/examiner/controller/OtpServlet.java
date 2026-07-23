package examiner.controller;

import examiner.filter.ExaminerFilter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import shared.dto.ExamAccessOtpDTO;
import shared.model.ExaminerSchedule;
import shared.service.ExamAccessOtpService;
import shared.service.impl.ExamAccessOtpServiceImpl;

@WebServlet("/examiner/otp")
public class OtpServlet extends HttpServlet {

    private final ExamAccessOtpService otpService = new ExamAccessOtpServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !ExaminerFilter.isTheory(session)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Integer examId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        ExaminerSchedule schedule = (ExaminerSchedule) session.getAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE);
        if (examId == null || schedule == null || schedule.getExamSectionId() == null
                || schedule.getExamAreaId() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            ExamAccessOtpDTO otp = otpService.getCurrent(
                    examId, schedule.getExamSectionId(), schedule.getExamAreaId());
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control", "no-store");
            response.getWriter().write("{\"code\":\"" + otp.getCode()
                    + "\",\"expiresAt\":" + otp.getExpiresAtEpochSecond() + "}");
        } catch (IllegalStateException ex) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "OTP is not configured.");
        }
    }
}
