package auth.controller.general;

import auth.dto.UserDTO;
import shared.Attributes;
import shared.model.User;
import auth.service.AuthService;
import auth.service.impl.AuthServiceImpl;
import auth.util.AuthFlashUtil;
import static shared.util.FormatUtil.formatString;
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
        AuthFlashUtil.promoteLoginFlash(request);
        AuthFlashUtil.promoteRegistrationCredentials(request);

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
            forwardWithError(request, response, "Vui lòng nhập tên đăng nhập/email/số căn cước và mật khẩu.");
            return;
        }

        // authenticate user
        User user = authService.login(identifier, password);

        // invalid credentials
        if (user == null) {
            forwardWithError(request, response, "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
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
            forwardWithError(request, response, "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
            return;
        }

        // store session identity as UserDTO (no password hash)
        HttpSession session = request.getSession();
        session.setAttribute(Attributes.Session.USER, UserDTO.fromUser(user));

        // redirect to dashboard servlet (filter maps JSP URLs too)
        response.sendRedirect(request.getContextPath() + "/registrant/dashboard");
    }

    // forward back to login page with an error message
    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {
        AuthFlashUtil.forwardWithError(request, response, "/views/auth/general/login.jsp", errorMessage);
    }
}
