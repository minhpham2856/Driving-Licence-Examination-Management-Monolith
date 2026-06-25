package Controllers.Registrant;

import Models.User;
import Services.RegistrantSettingsService;
import Services.Impl.RegistrantSettingsServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/registrant/settings")
public class SettingsServlet extends HttpServlet {

    private static final String VIEW = "/views/registrant/settings.jsp";

    private final RegistrantSettingsService settingsService = new RegistrantSettingsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        settingsService.applySettingsView(user, request);
        RegistrantServletSupport.forwardView(request, response, VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }

        String formId = request.getParameter("formId");
        String error = null;

        if ("password".equals(formId)) {
            error = settingsService.changePassword(user,
                    request.getParameter("currentPassword"),
                    request.getParameter("newPassword"),
                    request.getParameter("confirmPassword"),
                    request.getSession());
        } else if ("deactivate".equals(formId)) {
            error = settingsService.deactivateAccount(user,
                    request.getParameter("confirmDeactivate") != null,
                    request.getSession());
            if (error == null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
        } else if ("prefs".equals(formId)) {
            error = settingsService.saveNotificationPrefs(request,
                    request.getParameter("emailResultsNotify") != null,
                    request.getParameter("passwordChangeNotify") != null);
        } else {
            error = "Yêu cầu không hợp lệ.";
        }

        if (error != null) {
            settingsService.applySettingsView(user, request);
            request.setAttribute("error", error);
            RegistrantServletSupport.forwardView(request, response, VIEW);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/registrant/settings?success=1");
    }
}
