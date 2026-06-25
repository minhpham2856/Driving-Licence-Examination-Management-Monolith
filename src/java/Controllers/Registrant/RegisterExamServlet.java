package Controllers.Registrant;

import Models.User;
import Services.RegistrantRegisterExamService;
import Services.Impl.RegistrantRegisterExamServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/registrant/register-exam")
public class RegisterExamServlet extends HttpServlet {

    private static final String CONFIRM_PARAM = "confirmRegistration";

    private final RegistrantRegisterExamService registerExamService = new RegistrantRegisterExamServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        registerExamService.loadRegisterExamPage(user, request);
        RegistrantServletSupport.forwardView(request, response, "/views/registrant/register-exam.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        if (!"1".equals(request.getParameter(CONFIRM_PARAM))) {
            response.sendRedirect(registerExamService.buildRegisterExamPageUrl(request, "register-exam-summary"));
            return;
        }
        String error = registerExamService.submitRegistration(user, request);
        if (error != null) {
            RegistrantServletSupport.setFlash(request.getSession(),
                    RegistrantRegisterExamServiceImpl.FLASH_ERROR_ATTR, error);
            response.sendRedirect(registerExamService.buildRegisterExamPageUrl(request, "register-exam-summary"));
            return;
        }
        response.sendRedirect(request.getContextPath() + "/registrant/my-exams?success=registered");
    }
}
