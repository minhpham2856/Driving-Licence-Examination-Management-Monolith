package Controllers.Registrant;

import Models.User;
import Services.Impl.RegistrantRegisterExamServiceImpl;
import Services.RegistrantRegisterExamService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet đăng ký đợt thi — chỉ tạo ExamRegistration, không thanh toán.
 * <p>Thu lệ phí: actor Payment/SEPay tích hợp riêng (xem Integration.Sepay).</p>
 */
@WebServlet("/registrant/register-exam")
public class RegisterExamServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(RegisterExamServlet.class.getName());

    private final RegistrantRegisterExamService registerExamService = new RegistrantRegisterExamServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để đăng ký thi.");
        if (user == null) {
            return;
        }

        RegistrantAuth.transferFlash(request, "errorMessage", "errorMessage");
        registerExamService.populateRegisterPage(request, user,
                request.getParameter("licenceSelect"),
                request.getParameter("sessionSelect"));
        request.getRequestDispatcher("/views/registrant/register-exam.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để đăng ký thi.");
        if (user == null) {
            return;
        }

        try {
            String error = registerExamService.registerExam(request, user);

            if (error != null) {
                LOG.warning("Register exam failed userId=" + user.getId() + ": " + error);
                showRegisterForm(request, response, user, error);
                return;
            }

            request.getSession().setAttribute("successMessage",
                    "Đăng ký đợt thi thành công. Vui lòng đến quầy thu ngân để nộp lệ phí.");
            response.sendRedirect(request.getContextPath() + "/registrant/my-exams");

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Register exam exception userId=" + user.getId(), ex);
            showRegisterForm(request, response, user,
                    "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        }
    }

    private void showRegisterForm(HttpServletRequest request, HttpServletResponse response, User user, String error)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        registerExamService.populateRegisterPage(request, user,
                request.getParameter("licenceSelect"),
                request.getParameter("sessionSelect"));
        request.getRequestDispatcher("/views/registrant/register-exam.jsp").forward(request, response);
    }
}
