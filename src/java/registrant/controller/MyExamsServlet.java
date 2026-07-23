package registrant.controller;

import auth.dto.UserDTO;
import registrant.service.RegistrantMyExamsService;
import registrant.service.impl.RegistrantMyExamsServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Danh sách ca thi của tôi — {@code GET /registrant/my-exams}.
 * <p>
 * Join Profile → Candidate (CCCD) → ExamEnrollment → Exam + Payment
 * → list {@code myExamList}, điểm/SBD (khi staff đã tạo ngày thi).
 * Flash {@code success=registered} = vừa chọn RegistrationDates (chưa có SBD chính thức).
 * <p>
 * Registrant không gửi yêu cầu hủy lịch thi từ portal.
 */
@WebServlet("/registrant/my-exams")
public class MyExamsServlet extends HttpServlet {

    private static final String VIEW = "/views/registrant/my-exams.jsp";

    private final RegistrantMyExamsService myExamsService = new RegistrantMyExamsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        myExamsService.copyMyExamsToRequest(user, request, request.getParameter("examId"));

        if ("registered".equals(request.getParameter("success"))) {
            request.setAttribute("successMessage",
                    "Đã ghi nhận nguyện vọng ngày thi. Trạng thái: nguyện vọng — chờ lịch chính thức. Số báo danh và giờ ca sẽ cập nhật tại đây khi trung tâm công bố.");
        }

        RegistrantServletSupport.forwardView(request, response, VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/registrant/my-exams");
    }
}
