package registrant.controller;

import auth.dto.UserDTO;
import registrant.service.RegistrantSettingsService;
import registrant.service.impl.RegistrantSettingsServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Cài đặt GET/POST /registrant/settings — POST theo formId: đổi mật khẩu / vô hiệu hóa tài khoản. */
@WebServlet("/registrant/settings")
public class SettingsServlet extends HttpServlet {

    private static final String VIEW = "/views/registrant/settings.jsp";

    private final RegistrantSettingsService settingsService = new RegistrantSettingsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        settingsService.applySettingsView(user, request);
        RegistrantServletSupport.forwardView(request, response, VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO user = RegistrantAuth.requireRegistrant(request, response);
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
