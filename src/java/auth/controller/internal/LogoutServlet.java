package auth.controller.internal;

import shared.Attributes;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/staff/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processLogout(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processLogout(request, response);
    }

    private void processLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // get session (no new session if null)
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(Attributes.Session.SUCCESS_MESSAGE, "Bạn đã đăng xuất.");
        response.sendRedirect(request.getContextPath() + "/staff/login");
    }
}
