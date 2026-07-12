package auth.controller.general;

import shared.model.User;
import auth.service.AuthService;
import auth.service.impl.AuthServiceImpl;
import static auth.util.FormatUtil.formatString;
import shared.enums.RoleType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // transfer flash messages from session to request
        sessionToRequest(request);

        // show login page
        request.getRequestDispatcher("/views/auth/general/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // get and normalize inputs
        String identifier = formatString(request.getParameter("identifier"));
        String password = formatString(request.getParameter("password"));

        // validate required fields
        if (identifier == null || password == null) {
            forwardWithError(request, response, "Vui lÃ²ng nháº­p tÃªn Ä‘Äƒng nháº­p/email/sá»‘ cÄƒn cÆ°á»›c vÃ  máº­t kháº©u.");
            return;
        }

        // authenticate user
        User user = authService.login(identifier, password);

        // invalid credentials
        if (user == null) {
            forwardWithError(request, response, "TÃªn Ä‘Äƒng nháº­p/email/sá»‘ cÄƒn cÆ°á»›c hoáº·c máº­t kháº©u khÃ´ng chÃ­nh xÃ¡c.");
            return;
        }

        // continue login process
        handleSuccessfulLogin(request, response, user);
    }

    // complete login after successful authentication
    private void handleSuccessfulLogin(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException, ServletException {

        // only registrants can log in here
        if (RoleType.fromValue(user.getRole().getRoleName()) != RoleType.REGISTRANT) {
            forwardWithError(request, response, "TÃªn Ä‘Äƒng nháº­p/email/sá»‘ cÄƒn cÆ°á»›c hoáº·c máº­t kháº©u khÃ´ng chÃ­nh xÃ¡c.");
            return;
        }

        // store logged in user
        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        // redirect to dashboard
        response.sendRedirect(request.getContextPath() + "/views/registrant/dashboard.jsp");
    }

    // forward back to login page with an error message
    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {

        request.setAttribute("error", errorMessage);
        request.getRequestDispatcher("/views/auth/general/login.jsp").forward(request, response);
    }

    // move session messages to request scope
    private void sessionToRequest(HttpServletRequest request) {
        HttpSession session = request.getSession();

        // transfer flash messages
        transferAttribute(request, session, "successMessage", "success");
        transferAttribute(request, session, "errorMessage", "error");

        // transfer registration credentials
        transferAttribute(request, session, "registrationUsername", "registrationUsername");
        transferAttribute(request, session, "registrationPassword", "registrationPassword");
    }

    // move one attribute from session to request
    private void transferAttribute(HttpServletRequest request, HttpSession session,
            String sessionAttribute, String requestAttribute) {
        Object value = session.getAttribute(sessionAttribute);

        if (value != null) {
            request.setAttribute(requestAttribute, value);
            session.removeAttribute(sessionAttribute);
        }
    }
}

