package Controllers.Registrant;



import Models.User;

import Services.Impl.RegistrantMyExamsServiceImpl;

import Services.RegistrantMyExamsService;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;



/**
 * Danh sách lịch thi đã đăng ký (filter, phân trang).
 * <p>URL: GET /registrant/my-exams?status=&amp;q=&amp;page=</p>
 * <p>Chi tiết: {@link MyExamDetailServlet}</p>
 */
@WebServlet("/registrant/my-exams")
public class MyExamsServlet extends HttpServlet {

    private final RegistrantMyExamsService myExamsService = new RegistrantMyExamsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để xem lịch thi.");
        if (user == null) {
            return;
        }

        String examId = request.getParameter("examId");
        if (examId != null && !examId.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/registrant/my-exams/detail?examId=" + examId.trim());
            return;
        }

        RegistrantAuth.transferFlash(request, "successMessage", "success");
        RegistrantAuth.transferFlash(request, "errorMessage", "errorMessage");

        myExamsService.populateExamList(request, user);
        request.getRequestDispatcher("/views/registrant/my-exams.jsp").forward(request, response);
    }
}
