package controller.staff.exam;

import dao.ExamSessionDAO;
import dao.impl.ExamSessionDAOImpl;
import dto.exam.SessionDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import util.Utf8EncodingHelper;

@WebServlet("/views/staff/examstaff/select-session")
public class SessionSelectServlet extends HttpServlet {

    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

    // Xu ly yeu cau GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleSelect(request, response);
    }

    // Xu ly yeu cau POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleSelect(request, response);
    }

    private void handleSelect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Utf8EncodingHelper.apply(request, response);
        ExamStaffViewHelper.applyNoCacheHeaders(response);
        HttpSession httpSession = request.getSession();
        try {
            List<SessionDTO> allSessions = ExamStaffViewHelper.loadAllSessions(sessionDAO);

            Integer previousExamId = (Integer) httpSession.getAttribute("selectedExamId");
            Integer previousSessionId = (Integer) httpSession.getAttribute("selectedSessionId");

            int examId = ExamStaffViewHelper.applySessionIdFromRequest(request, httpSession, allSessions, sessionDAO);

            if (examId <= 0) {
                int failedSessionId = ExamStaffViewHelper.parseSessionIdParam(request);
                String param = failedSessionId > 0 ? String.valueOf(failedSessionId) : request.getParameter("examId");
                httpSession.setAttribute("sessionSelectError",
                        "Không tìm thấy kỳ thi" + (param != null ? " (id=" + param + ")." : "."));
                response.sendRedirect(ExamStaffViewHelper.resolveSafeRedirect(request, "/views/staff/examstaff/dashboard"));
                return;
            }

            Integer pickedSessionId = (Integer) httpSession.getAttribute("selectedSessionId");
            int sessionId = pickedSessionId != null ? pickedSessionId : 0;

            if (previousExamId != null && previousExamId > 0 && !previousExamId.equals(examId)) {
                ExamStaffViewHelper.clearProcedureStateOnExamChange(request, httpSession, previousExamId,
                        previousSessionId, examId, sessionId);
            } else if (previousSessionId != null && sessionId > 0 && !previousSessionId.equals(sessionId)) {
                ExamStaffViewHelper.clearCandidateCache(httpSession);
            }

            String webRoot = request.getServletContext().getRealPath("/");
            ExamStaffViewHelper.refreshCandidateQueue(httpSession, examId, sessionId, webRoot, allSessions);

            httpSession.setAttribute("examStaffQueueRevision", System.currentTimeMillis());
            httpSession.setAttribute("examStaffSessionJustChanged", Boolean.TRUE);
            httpSession.setAttribute("sessionSelectMsg", "Đã chọn kỳ thi mới.");

            String redirect = ExamStaffViewHelper.resolveSafeRedirect(request, "/views/staff/examstaff/dashboard");
            redirect = ExamStaffViewHelper.stripQueryString(redirect);

            int pickerSessionId = ExamStaffViewHelper.parseSessionIdParam(request);
            if (pickerSessionId > 0) {
                redirect = ExamStaffViewHelper.upsertQueryParam(redirect, "sessionId", String.valueOf(pickerSessionId));
            } else if (sessionId > 0) {
                redirect = ExamStaffViewHelper.upsertQueryParam(redirect, "sessionId", String.valueOf(sessionId));
            }

            redirect = ExamStaffViewHelper.upsertQueryParam(redirect, "_", String.valueOf(System.currentTimeMillis()));
            response.sendRedirect(redirect);
        } catch (Exception e) {
            e.printStackTrace();
            httpSession.setAttribute("sessionSelectError",
                    "Không đổi được kỳ thi: " + (e.getMessage() != null ? e.getMessage() : "lỗi không xác định"));
            response.sendRedirect(ExamStaffViewHelper.resolveSafeRedirect(request, "/views/staff/examstaff/dashboard"));
        }
    }
}
