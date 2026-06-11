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
 * Chi tiết kỳ thi và kết quả — tách khỏi danh sách lịch thi.
 * <p>URL: GET /registrant/my-exams/detail?examId={registrationId}</p>
 */
@WebServlet("/registrant/my-exams/detail")
public class MyExamDetailServlet extends HttpServlet {

    private final RegistrantMyExamsService myExamsService = new RegistrantMyExamsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để xem chi tiết kỳ thi.");
        if (user == null) {
            return;
        }

        RegistrantAuth.transferFlash(request, "successMessage", "success");
        myExamsService.populateExamDetail(request, user);
        request.getRequestDispatcher("/views/registrant/my-exam-detail.jsp").forward(request, response);
    }
}
